import { useQuery } from '@tanstack/react-query';
import { getBooking, getPayments, getTickets } from '../../api/bookings';

export const bookingKeys = { detail: (id: number) => ['booking', id] as const, payments: (id: number) => ['booking', id, 'payments'] as const, tickets: (id: number) => ['booking', id, 'tickets'] as const };
export function useBooking(id: number) { return useQuery({ queryKey: bookingKeys.detail(id), queryFn: () => getBooking(id), enabled: Number.isSafeInteger(id) && id > 0, refetchInterval: (query) => query.state.data?.status === 'CONFIRMED' ? false : 5_000 }); }
export function usePayments(id: number) { return useQuery({ queryKey: bookingKeys.payments(id), queryFn: () => getPayments(id), enabled: Number.isSafeInteger(id) && id > 0, refetchInterval: 5_000 }); }
export function useTickets(id: number, enabled: boolean) { return useQuery({ queryKey: bookingKeys.tickets(id), queryFn: () => getTickets(id), enabled: enabled && Number.isSafeInteger(id) && id > 0 }); }
