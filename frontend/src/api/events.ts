import type { AdminStatistics, EventItem, EventReview, EventStatus, EventType, Favourite, OrganizerStatistics, Page, ReviewEligibility, SeatAvailability } from '../types/api';
import { http } from './http';

export interface EventFilters {
  city?: string; type?: EventType | ''; from?: string; to?: string; venueId?: string;
  status?: EventStatus | ''; minPrice?: string; maxPrice?: string; page?: number; size?: number; sort?: string;
}
export async function getEvents(filters: EventFilters = {}) {
  const params = Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== '' && value !== undefined && value !== null));
  const { data } = await http.get<Page<EventItem>>('/events', { params });
  return data;
}
export async function getEvent(id: number) {
  const { data } = await http.get<EventItem>(`/events/${id}`);
  return data;
}
export async function getAvailability(eventId: number) {
  const { data } = await http.get<SeatAvailability[]>(`/events/${eventId}/availability`);
  return data;
}
export async function getMyEvents(page = 0, size = 100) {
  const { data } = await http.get<Page<EventItem>>('/events/mine', { params: { page, size, sort: 'startTime,asc' } }); return data;
}
export interface EventInput { name: string; description?: string; eventType: EventType; language?: string; organizer?: string; imageUrl?: string; startTime: string; duration?: string; venueId: number; screenId: number; organizerId?: number }
export async function createEvent(input: EventInput) { const { data } = await http.post<EventItem>('/events', input); return data; }
export async function updateEvent(id: number, input: EventInput) { const { data } = await http.put<EventItem>(`/events/${id}`, input); return data; }
export async function updateEventStatus(id: number, status: EventStatus) { const { data } = await http.patch<EventItem>(`/events/${id}/status`, { status }); return data; }
export async function deleteEvent(id: number) { await http.delete(`/events/${id}`); }
export async function getReviews(eventId: number, page = 0) { const {data}=await http.get<Page<EventReview>>(`/events/${eventId}/reviews`,{params:{page,size:10,sort:'createdAt,desc'}});return data; }
export async function getReviewEligibility(eventId: number) { const {data}=await http.get<ReviewEligibility>(`/events/${eventId}/reviews/eligibility`);return data; }
export async function saveReview(eventId:number, rating:number, comment:string, reviewId?:number) { const path=reviewId?`/reviews/${reviewId}`:`/events/${eventId}/reviews`;const {data}=await http.request<EventReview>({method:reviewId?'put':'post',url:path,data:{rating,comment}});return data; }
export async function deleteReview(reviewId:number) { await http.delete(`/reviews/${reviewId}`); }
export async function getWishlist(page=0) { const {data}=await http.get<Page<Favourite>>('/wishlist',{params:{page,size:20,sort:'createdAt,desc'}});return data; }
export async function addFavourite(eventId:number) { const {data}=await http.put<Favourite>(`/wishlist/${eventId}`);return data; }
export async function removeFavourite(eventId:number) { await http.delete(`/wishlist/${eventId}`); }
export async function getOrganizerStatistics() { const {data}=await http.get<OrganizerStatistics>('/organizer/statistics',{params:{page:0,size:20}});return data; }
export async function getAdminStatistics() { const {data}=await http.get<AdminStatistics>('/admin/statistics');return data; }
