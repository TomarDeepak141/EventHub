import type { Booking, Page, Payment, Reservation, Ticket, TicketVerification } from '../types/api';
import { http } from './http';

export async function reserveSeats(eventId: number, seatIds: number[]) {
  const { data } = await http.post<Reservation>(`/events/${eventId}/reservations`, { seatIds }); return data;
}
export async function cancelReservation(eventId: number, token: string) {
  await http.delete(`/events/${eventId}/reservations/${encodeURIComponent(token)}`);
}
export async function createBooking(eventId: number, reservationToken: string) {
  const { data } = await http.post<Booking>(`/events/${eventId}/bookings`, { reservationToken }); return data;
}
export async function getBooking(id: number) {
  const { data } = await http.get<Booking>(`/bookings/${id}`); return data;
}
export async function getBookings(page = 0, size = 20, status?: string) {
  const { data } = await http.get<Page<Booking>>('/bookings', { params: { page, size, sort: 'createdAt,desc', ...(status?{status}:{}) } }); return data;
}
export async function cancelBooking(id: number) {
  const { data } = await http.delete<Booking>(`/bookings/${id}`); return data;
}
export async function getPayments(bookingId: number) {
  const { data } = await http.get<Payment[]>(`/bookings/${bookingId}/payments`); return data;
}
export async function initiatePayment(bookingId: number, idempotencyKey: string) {
  const { data } = await http.post<Payment>(`/bookings/${bookingId}/payments`, { idempotencyKey }); return data;
}
export async function completePayment(paymentId: number, outcome: 'SUCCEED' | 'FAIL') {
  const { data } = await http.post<Payment>(`/payments/${paymentId}/complete`, { outcome }); return data;
}
export async function cancelPayment(paymentId: number) {
  const { data } = await http.post<Payment>(`/payments/${paymentId}/cancel`); return data;
}
export async function getTickets(bookingId: number) {
  const { data } = await http.get<Ticket[]>(`/bookings/${bookingId}/tickets`); return data;
}
export async function verifyTicket(ticketReference: string) {
  const { data } = await http.post<TicketVerification>('/tickets/verify', { ticketReference }); return data;
}
