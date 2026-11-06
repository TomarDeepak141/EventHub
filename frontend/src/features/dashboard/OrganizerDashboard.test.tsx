import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { createEvent, getMyEvents, getOrganizerStatistics } from '../../api/events';
import { getSeatCategories, getScreens, getVenues } from '../../api/catalog';
import { verifyTicket } from '../../api/bookings';
import { OrganizerDashboard } from './OrganizerDashboard';

vi.mock('../../api/events', () => ({
  createEvent: vi.fn(), updateEvent: vi.fn(), deleteEvent: vi.fn(), updateEventStatus: vi.fn(),
  getMyEvents: vi.fn(), getOrganizerStatistics: vi.fn(),
}));
vi.mock('../../api/catalog', () => ({ getVenues: vi.fn(), getScreens: vi.fn(), getSeatCategories: vi.fn() }));
vi.mock('../../api/bookings', () => ({ verifyTicket: vi.fn() }));

const emptyPage = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 100, first: true, last: true, empty: true };
const statistics = { ownedEvents: 0, confirmedBookings: 0, confirmedBookingValue: 0, ticketsIssued: 0, reviews: 0, averageRating: 0, events: emptyPage };

function renderDashboard() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } });
  return render(<QueryClientProvider client={client}><MemoryRouter><OrganizerDashboard/></MemoryRouter></QueryClientProvider>);
}

describe('organizer event management', () => {
  it('creates an owned event using existing venue and screen while showing seat category pricing', async () => {
    vi.mocked(getMyEvents).mockResolvedValue(emptyPage as never);
    vi.mocked(getOrganizerStatistics).mockResolvedValue(statistics as never);
    vi.mocked(getVenues).mockResolvedValue({ ...emptyPage, content: [{ id: 5, name: 'Civic Hall', city: 'Pune' }] } as never);
    vi.mocked(getScreens).mockResolvedValue([{ id: 7, name: 'Main Room', venueId: 5, venueName: 'Civic Hall' }] as never);
    vi.mocked(getSeatCategories).mockResolvedValue([{ id: 2, name: 'Premium', price: 125 }] as never);
    vi.mocked(verifyTicket).mockResolvedValue({} as never);
    vi.mocked(createEvent).mockResolvedValue({ id: 12, name: 'A New Play' } as never);
    renderDashboard();

    fireEvent.change(await screen.findByLabelText('Event name'), { target: { value: 'A New Play' } });
    fireEvent.change(screen.getByLabelText('Event type'), { target: { value: 'THEATRE' } });
    fireEvent.change(screen.getByLabelText('Start time'), { target: { value: '2027-04-12T19:30' } });
    fireEvent.change(screen.getByLabelText('Venue'), { target: { value: '5' } });
    fireEvent.change(screen.getByLabelText('Screen'), { target: { value: '7' } });
    expect(screen.getByText('Premium')).toBeInTheDocument();
    expect(screen.getByText('125.00')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: /create event/i }));

    await waitFor(() => expect(createEvent).toHaveBeenCalledWith(expect.objectContaining({
      name: 'A New Play', eventType: 'THEATRE', startTime: '2027-04-12T19:30',
      venueId: 5, screenId: 7, duration: 'PT2H',
    })));
    expect(await screen.findByRole('status')).toHaveTextContent('A New Play saved.');
  });
});
