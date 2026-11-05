import { Link, useParams } from 'react-router-dom';
import { ArrowLeft, CheckCircle2, Ticket } from 'lucide-react';
import { getApiError } from '../../../api/http';
import { ErrorState, LoadingState } from '../../../components/ui/States';
import { useBooking, useTickets } from '../hooks';

export function BookingConfirmationPage() {
  const id = Number(useParams().bookingId); const booking = useBooking(id); const tickets = useTickets(id, booking.data?.status === 'CONFIRMED');
  if (!Number.isSafeInteger(id) || id < 1) return <div className="section-wrap booking-wrap"><ErrorState message="Booking ID must be a positive whole number."/></div>;
  if (booking.isPending) return <div className="section-wrap booking-wrap"><LoadingState label="Checking booking status"/></div>;
  if (booking.isError || !booking.data) return <div className="section-wrap booking-wrap"><ErrorState message={getApiError(booking.error)} retry={() => void booking.refetch()}/></div>;
  if (booking.data.status !== 'CONFIRMED') return <main className="section-wrap booking-wrap"><Link className="back-link" to="/events"><ArrowLeft size={16}/> Explore events</Link><div className="payment-success payment-pending"><span><Ticket size={30}/></span><p className="eyebrow">Booking #{booking.data.id}</p><h1>This booking is {booking.data.status.toLowerCase().replaceAll('_',' ')}.</h1><p>EventHub has not confirmed this booking. No ticket is available yet.</p><Link className="button button-primary" to={`/checkout/${id}`}>Return to checkout</Link></div></main>;
  return <main className="section-wrap booking-wrap"><div className="payment-success"><span><CheckCircle2 size={30}/></span><p className="eyebrow">Booking #{booking.data.id} · confirmed</p><h1>It’s happening.</h1><p>EventHub confirmed your booking. Keep these ticket references ready at entry.</p></div>
    <section className="ticket-list"><div className="ticket-list-heading"><div><p className="eyebrow">Your tickets</p><h2>{booking.data.items.length} {booking.data.items.length === 1 ? 'ticket' : 'tickets'}</h2></div><strong>Total {Number(booking.data.totalAmount).toFixed(2)}</strong></div>
      {tickets.isPending ? <LoadingState label="Loading tickets"/> : tickets.isError ? <ErrorState message={getApiError(tickets.error)} retry={() => void tickets.refetch()}/> : tickets.data?.length ? tickets.data.map((ticket) => <article className="ticket-card" id={`ticket-${ticket.ticketReference}`} key={ticket.ticketReference}><span className="ticket-icon"><Ticket size={20}/></span><div><p className="eyebrow">Seat {ticket.seatIdentifier}</p><h3>Ticket reference</h3><code>{ticket.ticketReference}</code><p>Use this ticket at the venue. It expires {new Date(ticket.expiresAt).toLocaleString()}.</p></div><a className="button button-secondary" href={`#ticket-${ticket.ticketReference}`} aria-label={`Ticket for seat ${ticket.seatIdentifier}`}>Open ticket</a><details className="ticket-payload"><summary>Show QR payload</summary><code>{ticket.qrPayload}</code></details></article>) : <p className="booking-alert" role="status">No tickets were returned yet. Refresh to check again.</p>}
    </section><div className="confirmation-links"><Link className="button button-secondary" to="/profile">Go to profile</Link><Link className="button button-primary" to="/events">Find another event</Link></div>
  </main>;
}
