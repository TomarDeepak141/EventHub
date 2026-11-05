import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { CalendarDays, ChevronRight, CircleCheck, Clock3, Heart, Ticket, UserRound } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { getBookings } from '../../api/bookings';
import { getWishlist, removeFavourite } from '../../api/events';
import { getMyProfile } from '../../api/users';
import { getApiError } from '../../api/http';
import { ErrorState, LoadingState, EmptyState } from '../../components/ui/States';
import { Pagination } from '../../components/ui/Pagination';

const statusClass = (status: string) => `status-chip status-${status.toLowerCase()}`;
export function CustomerDashboard() {
  const [page,setPage]=useState(0);
  const [status,setStatus]=useState('');const client=useQueryClient();
  const profile = useQuery({ queryKey: ['profile'], queryFn: getMyProfile });
  const bookings = useQuery({ queryKey: ['bookings', 'mine',page,status], queryFn: () => getBookings(page,20,status||undefined) });
  const wishlist=useQuery({queryKey:['wishlist'],queryFn:()=>getWishlist()});
  const remove=useMutation({mutationFn:removeFavourite,onSuccess:()=>client.invalidateQueries({queryKey:['wishlist']})});
  if (profile.isPending || bookings.isPending || wishlist.isPending) return <div className="section-wrap dashboard-wrap"><LoadingState label="Loading your dashboard"/></div>;
  if (profile.isError || bookings.isError || wishlist.isError || !profile.data || !bookings.data) return <div className="section-wrap dashboard-wrap"><ErrorState message={getApiError(profile.error ?? bookings.error ?? wishlist.error)} retry={() => { void profile.refetch(); void bookings.refetch();void wishlist.refetch(); }}/></div>;
  const rows = bookings.data.content; const confirmed = rows.filter((booking) => booking.status === 'CONFIRMED').length; const cancelled = rows.filter((booking) => booking.status === 'CANCELLED').length;
  return <main className="section-wrap dashboard-wrap"><div className="dashboard-intro"><div><p className="eyebrow">Customer dashboard</p><h1>Welcome back, <em>{profile.data.name.split(' ')[0]}.</em></h1><p>Your plans, tickets, and bookings in one place.</p></div><Link className="button button-primary" to="/profile"><UserRound size={16}/> Edit profile</Link></div>
    <section className="dashboard-stats" aria-label="Booking summary"><article><span className="dash-icon"><CalendarDays size={18}/></span><div><b>{bookings.data.totalElements}</b><span>Total bookings</span></div></article><article><span className="dash-icon"><CircleCheck size={18}/></span><div><b>{confirmed}</b><span>Confirmed on this page</span></div></article><article><span className="dash-icon"><Clock3 size={18}/></span><div><b>{cancelled}</b><span>Cancelled on this page</span></div></article></section>
    <section className="dashboard-section"><div className="dashboard-section-heading"><div><p className="eyebrow">Your activity</p><h2>Booking history</h2></div><div className="customer-dashboard-actions"><label>Filter status <select value={status} onChange={(e)=>{setStatus(e.target.value);setPage(0);}}><option value="">All statuses</option>{['PENDING','PAYMENT_PENDING','CONFIRMED','FAILED','CANCELLED','EXPIRED'].map((value)=><option key={value}>{value}</option>)}</select></label><Link className="quiet-link" to="/events">Find another event <ChevronRight size={15}/></Link></div></div>
      {bookings.data.empty || rows.length === 0 ? <EmptyState title="No bookings yet" description="When you reserve tickets, they’ll be collected here."/> : <div className="dashboard-list">{rows.map((booking) => <Link className="dashboard-row" key={booking.id} to={`/dashboard/customer/bookings/${booking.id}`}><span className="dash-row-icon"><Ticket size={18}/></span><span className="dashboard-row-main"><b>Booking #{booking.id}</b><small>Event #{booking.eventId} · {booking.items.length} {booking.items.length === 1 ? 'seat' : 'seats'} · {Number(booking.totalAmount).toFixed(2)}</small></span><span className={statusClass(booking.status)}>{booking.status.replaceAll('_',' ')}</span><ChevronRight size={17}/></Link>)}</div>}
      <Pagination page={bookings.data.number} totalPages={bookings.data.totalPages} onChange={setPage}/>
    </section>
    <section className="dashboard-section"><div className="dashboard-section-heading"><div><p className="eyebrow">Saved for later</p><h2>Wishlist</h2></div><Heart size={18}/></div>{wishlist.data.empty?<EmptyState title="Your wishlist is empty" description="Save events you want to come back to."/>:<div className="dashboard-list">{wishlist.data.content.map(({event,eventId})=><article className="dashboard-row" key={eventId}><span className="dash-row-icon"><Heart size={17}/></span><Link className="dashboard-row-main" to={`/events/${eventId}`}><b>{event.name}</b><small>{new Date(event.startTime).toLocaleString()} · {event.venueName}, {event.city}</small></Link><button className="button button-ghost" disabled={remove.isPending} onClick={()=>remove.mutate(eventId)}>Remove</button></article>)}</div>}{remove.isError&&<p role="alert" className="booking-alert">{getApiError(remove.error)}</p>}</section>
  </main>;
}
