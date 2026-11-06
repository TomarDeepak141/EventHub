import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { getBooking, getPayments, initiatePayment } from '../../../api/bookings';
import { CheckoutPage } from './CheckoutPage';

vi.mock('../../../api/bookings', () => ({ getBooking: vi.fn(), getPayments: vi.fn(), initiatePayment: vi.fn(), completePayment: vi.fn(), cancelPayment: vi.fn(), cancelBooking: vi.fn() }));

const booking = { id: 31, eventId: 7, status: 'PENDING', totalAmount: 50, expiresAt: '2027-01-01T19:00:00Z', createdAt: '2026-11-04T00:00:00', items: [{ id: 1, seatId: 1, seatIdentifier: 'A1', priceAtBooking: 25 }, { id: 2, seatId: 2, seatIdentifier: 'A2', priceAtBooking: 25 }] };
const payment = { id: 501, bookingId: 31, provider: 'MOCK', status: 'INITIATED', amount: 50, createdAt: '2026-11-04T00:00:00', updatedAt: '2026-11-04T00:00:00' };
let attempt: typeof payment | null = null;
function renderPage() { const client = new QueryClient({ defaultOptions: { queries: { retry: false } } }); return render(<QueryClientProvider client={client}><MemoryRouter initialEntries={['/checkout/31']}><Routes><Route path="/checkout/:bookingId" element={<CheckoutPage/>}/></Routes></MemoryRouter></QueryClientProvider>); }
beforeEach(() => { vi.clearAllMocks(); vi.stubEnv('VITE_ENABLE_MOCK_PAYMENTS', 'true'); localStorage.clear(); attempt = null; vi.mocked(getBooking).mockResolvedValue(booking as never); vi.mocked(getPayments).mockImplementation(async () => attempt ? [attempt] as never : []); vi.mocked(initiatePayment).mockImplementation(async () => { attempt = payment; return payment as never; }); });
afterEach(() => vi.unstubAllEnvs());

describe('checkout and mock payment', () => {
  it('renders the server booking summary and prevents duplicate payment initiation', async () => {
    let resolvePayment!: (value: typeof payment) => void;
    vi.mocked(initiatePayment).mockImplementation(() => new Promise((resolve) => { resolvePayment = (value) => { attempt = value; resolve(value as never); }; }));
    renderPage();
    expect(await screen.findByText('Booking summary')).toBeInTheDocument();
    expect(screen.getByText('50.00')).toBeInTheDocument();
    const start = screen.getByRole('button', { name: 'Start mock payment' });
    fireEvent.click(start); fireEvent.click(start);
    await waitFor(() => expect(initiatePayment).toHaveBeenCalledTimes(1));
    resolvePayment(payment); await screen.findByText(/Attempt #501/);
    expect(screen.getByText(/only the server booking status/i)).toBeInTheDocument();
  });

  it('does not offer mock payment controls when the build disables the sandbox', async () => {
    vi.stubEnv('VITE_ENABLE_MOCK_PAYMENTS', 'false');
    renderPage();
    expect(await screen.findByText(/payment processing is not configured/i)).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /start mock payment/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /simulate success/i })).not.toBeInTheDocument();
  });
});
