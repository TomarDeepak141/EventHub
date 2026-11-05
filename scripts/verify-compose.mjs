import { execFileSync } from 'node:child_process';
import { readFileSync } from 'node:fs';
import { randomUUID } from 'node:crypto';

const readEnv = () => {
  const values = {};
  for (const line of readFileSync(new URL('../.env', import.meta.url), 'utf8').split(/\r?\n/)) {
    const match = line.match(/^\s*([A-Z0-9_]+)\s*=\s*(.*?)\s*$/);
    if (match) values[match[1]] = match[2].replace(/^(['"])(.*)\1$/, '$2');
  }
  return values;
};
const env = readEnv();
const port = env.HTTP_PORT || '8088';
const base = `http://127.0.0.1:${port}`;
const required = ['EVENT_ID', 'CUSTOMER1_EMAIL', 'CUSTOMER1_PASSWORD', 'CUSTOMER2_EMAIL', 'CUSTOMER2_PASSWORD', 'ORGANIZER_EMAIL', 'ORGANIZER_PASSWORD'];
for (const name of required) if (!process.env[name]) throw new Error(`Set ${name} in the shell before running this disposable-stack verification.`);

const api = async (path, { token, method = 'GET', body } = {}) => {
  const response = await fetch(`${base}/api${path}`, {
    method,
    headers: { ...(body ? { 'Content-Type': 'application/json' } : {}), ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    ...(body ? { body: JSON.stringify(body) } : {}),
  });
  let data;
  try { data = await response.json(); } catch { data = undefined; }
  return { response, data };
};
const assert = (condition, message) => { if (!condition) throw new Error(message); };
const docker = (...args) => execFileSync('docker', ['compose', ...args], { encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] }).trim();
const parseTtlSeconds = (value = 'PT30S') => {
  const match = value.match(/^PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?$/);
  if (!match) throw new Error('SEAT_RESERVATION_TTL must use PT#H#M#S ISO-8601 notation.');
  return Number(match[1] || 0) * 3600 + Number(match[2] || 0) * 60 + Number(match[3] || 0);
};

console.log('Checking the Nginx frontend and proxied public API…');
const page = await fetch(base);
assert(page.ok && (await page.text()).includes('EventHub'), 'Frontend did not serve its EventHub shell.');
const health = await api('/actuator/health/readiness');
assert(health.response.ok && health.data?.status === 'UP', 'Nginx could not reach a healthy Spring API with database/Redis readiness.');
const publicEvents = await api('/events?page=0&size=100&status=BOOKING_OPEN');
assert(publicEvents.response.ok && Array.isArray(publicEvents.data?.content), 'Nginx API proxy did not return the Spring event page.');
const frontendEventRoute = await fetch(`${base}/events/${encodeURIComponent(process.env.EVENT_ID)}`);
assert(frontendEventRoute.ok && (await frontendEventRoute.text()).includes('EventHub'), 'Nginx did not serve the frontend event detail route.');

console.log('Checking successful Flyway migrations…');
const database = env.POSTGRES_DB || 'eventhub';
const dbUser = env.POSTGRES_USER || 'eventhub';
const migrationRows = docker('exec', '-T', 'postgres', 'psql', '-U', dbUser, '-d', database, '-Atc', 'SELECT count(*) || \'|\' || bool_and(success) FROM flyway_schema_history;');
const [migrationCount, migrationsSuccessful] = migrationRows.split('|');
assert(Number(migrationCount) >= 7 && migrationsSuccessful === 't', `Flyway history is incomplete or failed: ${migrationRows}`);

const registerOrReuse = async (name, email, password) => {
  const result = await api('/user/register', { method: 'POST', body: { name, email, password } });
  assert(result.response.status === 201 || result.response.status === 409, `Registration failed for a smoke-test account (${result.response.status}).`);
};
const login = async (email, password) => {
  const result = await api('/auth/login', { method: 'POST', body: { email, password } });
  assert(result.response.ok && result.data?.jwt, `Login failed for configured verification customer (${result.response.status}).`);
  return result.data;
};
await Promise.all([
  registerOrReuse('Smoke Customer One', process.env.CUSTOMER1_EMAIL, process.env.CUSTOMER1_PASSWORD),
  registerOrReuse('Smoke Customer Two', process.env.CUSTOMER2_EMAIL, process.env.CUSTOMER2_PASSWORD),
  registerOrReuse('Smoke Organizer', process.env.ORGANIZER_EMAIL, process.env.ORGANIZER_PASSWORD),
]);
const [customer1, customer2, organizer] = await Promise.all([
  login(process.env.CUSTOMER1_EMAIL, process.env.CUSTOMER1_PASSWORD),
  login(process.env.CUSTOMER2_EMAIL, process.env.CUSTOMER2_PASSWORD),
  login(process.env.ORGANIZER_EMAIL, process.env.ORGANIZER_PASSWORD),
]);
assert(customer1.role === 'CUSTOMER' && customer2.role === 'CUSTOMER', 'The two smoke-test users must be CUSTOMER accounts.');
assert(organizer.role === 'ORGANIZER', 'Promote the registered local smoke-test account to ORGANIZER and attach the event before running the ticket-scan test.');
const token1 = customer1.jwt;
const token2 = customer2.jwt;
const eventId = encodeURIComponent(process.env.EVENT_ID);
const availability = await api(`/events/${eventId}/availability`);
assert(availability.response.ok && Array.isArray(availability.data), 'Could not read event seat availability.');
assert(publicEvents.data.content.some((item) => item.id === Number(process.env.EVENT_ID)), 'The seeded event is not visible in public event browsing through Nginx.');
const seat = availability.data.find((item) => item.status === 'AVAILABLE');
assert(seat, 'The verification event has no AVAILABLE seat; prepare a disposable event with at least one free seat.');

console.log('Checking Redis lock TTL and automatic expiry…');
const ttlSeconds = parseTtlSeconds(env.SEAT_RESERVATION_TTL || 'PT5M');
assert(ttlSeconds >= 30 && ttlSeconds <= 900, 'Configure SEAT_RESERVATION_TTL between PT30S and PT15M for verification.');
const expiryReservation = await api(`/events/${eventId}/reservations`, { token: token1, method: 'POST', body: { seatIds: [seat.seatId] } });
assert(expiryReservation.response.status === 201, `Could not create TTL probe reservation (${expiryReservation.response.status}).`);
const redisKey = `eventhub:seat-lock:event:${process.env.EVENT_ID}:seat:${seat.seatId}`;
const remaining = Number(docker('exec', '-T', 'redis', 'redis-cli', 'TTL', redisKey));
assert(remaining > 0 && remaining <= ttlSeconds, `Redis lock TTL was not in the configured range: ${remaining}.`);
await new Promise((resolve) => setTimeout(resolve, (remaining + 2) * 1000));
const expiredAvailability = await api(`/events/${eventId}/availability`);
assert(expiredAvailability.data?.some((item) => item.seatId === seat.seatId && item.status === 'AVAILABLE'), 'Seat did not return to AVAILABLE after its Redis TTL elapsed.');

console.log('Checking concurrent Redis reservation safety…');
const race = await Promise.all([
  api(`/events/${eventId}/reservations`, { token: token1, method: 'POST', body: { seatIds: [seat.seatId] } }),
  api(`/events/${eventId}/reservations`, { token: token2, method: 'POST', body: { seatIds: [seat.seatId] } }),
]);
const winners = race.filter((item) => item.response.status === 201);
const conflicts = race.filter((item) => item.response.status === 409);
assert(winners.length === 1 && conflicts.length === 1, `Expected exactly one reservation winner and one 409 conflict; received ${race.map((item) => item.response.status).join(', ')}.`);

console.log('Checking booking, mock payment failure/retry, ticket issuance, and status refresh…');
const winnerToken = race[0].response.status === 201 ? token1 : token2;
const reservationToken = winners[0].data?.reservationToken;
assert(reservationToken, 'Reservation response did not contain its owner token.');
const booking = await api(`/events/${eventId}/bookings`, { token: winnerToken, method: 'POST', body: { reservationToken } });
assert(booking.response.status === 201 && booking.data?.id, `Booking creation failed (${booking.response.status}).`);
const failedPayment = await api(`/bookings/${booking.data.id}/payments`, { token: winnerToken, method: 'POST', body: { idempotencyKey: `compose-fail-${randomUUID()}` } });
assert(failedPayment.response.ok, `Mock payment initiation failed (${failedPayment.response.status}).`);
const failedCompletion = await api(`/payments/${failedPayment.data.id}/complete`, { token: winnerToken, method: 'POST', body: { outcome: 'FAIL' } });
assert(failedCompletion.response.ok && failedCompletion.data?.status === 'FAILED', 'Mock payment failure outcome was not persisted.');
const retryState = await api(`/bookings/${booking.data.id}`, { token: winnerToken });
assert(retryState.data?.status === 'PAYMENT_PENDING', 'Booking did not remain retryable after mock payment failure.');
const payment = await api(`/bookings/${booking.data.id}/payments`, { token: winnerToken, method: 'POST', body: { idempotencyKey: `compose-success-${randomUUID()}` } });
assert(payment.response.ok, `Mock payment retry initiation failed (${payment.response.status}).`);
const completion = await api(`/payments/${payment.data.id}/complete`, { token: winnerToken, method: 'POST', body: { outcome: 'SUCCEED' } });
assert(completion.response.ok && completion.data?.status === 'SUCCEEDED', `Mock payment success failed (${completion.response.status}).`);
const [refreshedBooking, tickets] = await Promise.all([
  api(`/bookings/${booking.data.id}`, { token: winnerToken }),
  api(`/bookings/${booking.data.id}/tickets`, { token: winnerToken }),
]);
assert(refreshedBooking.data?.status === 'CONFIRMED', 'Backend did not confirm booking after mock payment success.');
assert(tickets.response.ok && tickets.data?.length > 0 && tickets.data.every((ticket) => ticket.qrPayload?.startsWith('eventhub:ticket:')), 'Confirmed booking did not issue QR-safe tickets.');

console.log('Checking organizer ownership and one-time admission scan…');
const ticketReference = tickets.data[0].ticketReference;
const firstScan = await api('/tickets/verify', { token: organizer.jwt, method: 'POST', body: { ticketReference } });
assert(firstScan.response.ok && firstScan.data?.admitted === true && firstScan.data?.status === 'USED', 'Organizer did not admit the owned active ticket.');
const repeatedScan = await api('/tickets/verify', { token: organizer.jwt, method: 'POST', body: { ticketReference } });
assert(repeatedScan.response.ok && repeatedScan.data?.admitted === false && repeatedScan.data?.status === 'USED', 'A repeated scan was incorrectly admitted.');

console.log(`Docker verification passed. Migrations: ${migrationCount}; Redis TTL: ${remaining}s; exactly one race winner; payment failure/retry/success; booking ${booking.data.id} confirmed; organizer admitted once; ${tickets.data.length} ticket(s) issued.`);
