import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { getAvailability, getEvent } from '../../../api/events';
import { reserveSeats } from '../../../api/bookings';
import { SeatSelectionPage } from './SeatSelectionPage';

vi.mock('../../../api/events', () => ({ getAvailability: vi.fn(), getEvent: vi.fn() }));
vi.mock('../../../api/bookings', () => ({ reserveSeats: vi.fn(), cancelReservation: vi.fn(), createBooking: vi.fn() }));
vi.mock('../../auth/AuthProvider', () => ({ useAuth: () => ({ user: { role: 'CUSTOMER' } }) }));

const fixture = [
  { seatId: 1, identifier: 'A1', categoryId: 1, categoryName: 'Standard', price: 25, status: 'AVAILABLE' as const },
  { seatId: 2, identifier: 'A2', categoryId: 1, categoryName: 'Standard', price: 25, status: 'LOCKED' as const },
  { seatId: 3, identifier: 'A3', categoryId: 1, categoryName: 'Standard', price: 25, status: 'CONFIRMED_BOOKED' as const },
];
function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(<QueryClientProvider client={client}><MemoryRouter initialEntries={['/events/7/seats']}><Routes><Route path="/events/:eventId/seats" element={<SeatSelectionPage/>}/></Routes></MemoryRouter></QueryClientProvider>);
}
beforeEach(() => { vi.clearAllMocks(); vi.mocked(getEvent).mockResolvedValue({ id: 7, name: 'Summer Live', venueName: 'Hall', screenName: 'Main', city: 'Pune', startTime: '2027-01-01T19:00:00', eventType: 'CONCERT', eventStatus: 'BOOKING_OPEN' } as never); vi.mocked(getAvailability).mockResolvedValue(fixture); });

describe('seat selection and reservation', () => {
  it('shows all availability states, prices the selection from server data, then starts an expiry countdown', async () => {
    vi.mocked(reserveSeats).mockResolvedValue({ reservationToken: 'reserve-token', eventId: 7, seatIds: [1], expiresAt: new Date(Date.now() + 2500).toISOString() });
    renderPage();
    expect(await screen.findByRole('button', { name: /A1.*25.00.*available/i })).toBeEnabled();
    expect(screen.getByRole('button', { name: /A2.*locked/i })).toBeDisabled();
    expect(screen.getByRole('button', { name: /A3.*confirmed booked/i })).toBeDisabled();
    fireEvent.click(screen.getByRole('button', { name: /A1/ }));
    expect(screen.getByText('Total').parentElement).toHaveTextContent('25.00');
    fireEvent.click(screen.getByRole('button', { name: 'Reserve seats' }));
    expect(await screen.findByText(/Held for/)).toBeInTheDocument();
    expect(reserveSeats).toHaveBeenCalledWith(7, [1]);
    expect(await screen.findByText(/reservation expired/i, {}, { timeout: 4000 })).toBeInTheDocument();
  }, 10000);

  it('reports a reservation conflict and keeps a retry path available', async () => {
    vi.mocked(reserveSeats).mockRejectedValueOnce(new Error('Seat unavailable')).mockResolvedValueOnce({ reservationToken: 'retry-token', eventId: 7, seatIds: [1], expiresAt: new Date(Date.now() + 60000).toISOString() });
    renderPage(); await screen.findByRole('button', { name: /A1/ }); fireEvent.click(screen.getByRole('button', { name: /A1/ })); fireEvent.click(screen.getByRole('button', { name: 'Reserve seats' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Seat unavailable');
    await waitFor(() => expect(getAvailability).toHaveBeenCalledTimes(2));
    fireEvent.click(screen.getByRole('button', { name: 'Reserve seats' }));
    expect(await screen.findByText(/Held for/)).toBeInTheDocument();
  });
});
