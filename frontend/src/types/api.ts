export type Role = 'CUSTOMER' | 'ORGANIZER' | 'ADMIN';
export type EventType = 'MOVIE' | 'CONCERT' | 'SPORTS' | 'THEATRE' | 'STANDUP' | 'SEMINAR' | 'WORKSHOP' | 'OTHER';
export type EventStatus = 'DRAFT' | 'BOOKING_OPEN' | 'SOLD_OUT' | 'CANCELLED' | 'COMPLETED';
export type SeatStatus = 'AVAILABLE' | 'LOCKED' | 'CONFIRMED_BOOKED';

export interface LoginResponse { userId: number; email: string; role: Role; jwt: string; date: string }
export interface UserProfile { id: number; name: string; email: string; role: Role; active: boolean }
export interface VenueSummary { id?: number; venueName: string; city: string }
export interface EventItem {
  id: number; name: string; description?: string | null; eventType: EventType; eventStatus: EventStatus;
  language?: string | null; organizer?: string | null; imageUrl?: string | null; startTime: string;
  duration?: string | null; venueName: string; venueId?: number; city: string; screenId: number; screenName: string;
  organizerId?: number | null; organizerEmail?: string | null;
}
export interface Page<T> {
  content: T[]; totalElements: number; totalPages: number; number: number; size: number;
  first: boolean; last: boolean; empty: boolean;
}
export interface SeatAvailability {
  seatId: number; identifier: string; categoryId: number; categoryName: string; price: number;
  status: SeatStatus; expiresAt?: string | null;
}
export interface Reservation { reservationToken: string; eventId: number; seatIds: number[]; expiresAt: string }
export type BookingStatus = 'PENDING' | 'PAYMENT_PENDING' | 'CONFIRMED' | 'FAILED' | 'CANCELLED' | 'EXPIRED';
export interface BookingItem { id: number; seatId: number; seatIdentifier: string; priceAtBooking: number }
export interface Booking { id: number; eventId: number; status: BookingStatus; totalAmount: number; expiresAt: string; createdAt: string; items: BookingItem[] }
export type PaymentStatus = 'INITIATED' | 'PROCESSING' | 'SUCCEEDED' | 'FAILED' | 'CANCELLED' | 'REFUND_PENDING' | 'REFUNDED';
export interface Payment { id: number; bookingId: number; provider: string; status: PaymentStatus; amount: number; providerReference?: string | null; failureCode?: string | null; createdAt: string; updatedAt: string }
export interface Ticket { ticketReference: string; qrPayload: string; bookingId: number; eventId: number; seatIdentifier: string; status: string; expiresAt: string }
export interface TicketVerification { ticketReference: string; status: string; admitted: boolean; message: string }
export interface ApiErrorBody {
  timestamp?: string; status?: number; error?: string; message?: string; path?: string;
  errors?: Record<string, string>;
}
export interface EventReview { id: number; eventId: number; customerName: string; rating: number; comment: string; createdAt: string; updatedAt: string }
export interface ReviewEligibility { eligible: boolean; reason?: string | null; review?: EventReview | null }
export interface Favourite { id: number; eventId: number; event: EventItem; createdAt: string }
export interface EventStatistics { eventId: number; eventName: string; status: EventStatus; confirmedBookings: number; confirmedBookingValue: number; issuedTickets: number; reviewCount: number; averageRating: number }
export interface OrganizerStatistics { ownedEvents: number; confirmedBookings: number; confirmedBookingValue: number; ticketsIssued: number; reviews: number; averageRating: number; events: Page<EventStatistics> }
export interface AdminStatistics { activeUsers: number; customers: number; organizers: number; administrators: number; events: number; confirmedBookings: number; confirmedBookingValue: number; activeTickets: number; usedTickets: number; cancelledTickets: number; expiredTickets: number; reviews: number; averageRating: number; favourites: number }
