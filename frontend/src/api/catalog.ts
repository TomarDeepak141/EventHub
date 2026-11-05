import type { Page } from '../types/api';
import { http } from './http';

export interface Venue { id: number; name: string; city: string }
export interface Screen { id: number; name: string; venueId: number; venueName: string }
export interface SeatCategory { id: number; name: string; price: number }
export interface Seat { id: number; identifier: string; screenId: number; categoryId: number; categoryName: string; price: number }
export async function getVenues(page = 0) { const { data } = await http.get<Page<Venue>>('/venues', { params: { page, size: 100, sort: 'name,asc' } }); return data; }
export async function saveVenue(input: Pick<Venue, 'name' | 'city'>, id?: number) { const { data } = id ? await http.put<Venue>(`/venues/${id}`, input) : await http.post<Venue>('/venues', input); return data; }
export async function deleteVenue(id: number) { await http.delete(`/venues/${id}`); }
export async function getScreens() { const { data } = await http.get<Screen[]>('/screens'); return data; }
export async function saveScreen(input: { name: string; venueId: number }, id?: number) { const { data } = id ? await http.put<Screen>(`/screens/${id}`, input) : await http.post<Screen>('/screens', input); return data; }
export async function deleteScreen(id: number) { await http.delete(`/screens/${id}`); }
export async function getSeatCategories() { const { data } = await http.get<SeatCategory[]>('/seat-categories'); return data; }
export async function saveSeatCategory(input: { name: string; price: number }, id?: number) { const { data } = id ? await http.put<SeatCategory>(`/seat-categories/${id}`, input) : await http.post<SeatCategory>('/seat-categories', input); return data; }
export async function deleteSeatCategory(id: number) { await http.delete(`/seat-categories/${id}`); }
export async function getSeats(screenId: number) { const { data } = await http.get<Seat[]>('/seats', { params: { screenId } }); return data; }
export async function saveSeat(input: { identifier: string; screenId: number; categoryId: number }, id?: number) { const { data } = id ? await http.put<Seat>(`/seats/${id}`, input) : await http.post<Seat>('/seats', input); return data; }
export async function deleteSeat(id: number) { await http.delete(`/seats/${id}`); }
