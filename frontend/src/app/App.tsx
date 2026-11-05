import { lazy, Suspense, type ReactNode } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { AppShell } from '../components/layout/AppShell';
import { ProtectedRoute } from '../features/auth/ProtectedRoute';
import { LoadingState } from '../components/ui/States';
import '../features/booking/booking.css';
import '../features/dashboard/dashboard.css';

const HomePage=lazy(()=>import('../features/events/pages/HomePage').then((m)=>({default:m.HomePage})));
const EventsPage=lazy(()=>import('../features/events/pages/EventsPage').then((m)=>({default:m.EventsPage})));
const EventDetailPage=lazy(()=>import('../features/events/pages/EventDetailPage').then((m)=>({default:m.EventDetailPage})));
const LoginPage=lazy(()=>import('../features/auth/pages/LoginPage').then((m)=>({default:m.LoginPage})));
const RegisterPage=lazy(()=>import('../features/auth/pages/RegisterPage').then((m)=>({default:m.RegisterPage})));
const ProfilePage=lazy(()=>import('../features/profile/ProfilePage').then((m)=>({default:m.ProfilePage})));
const NotFoundPage=lazy(()=>import('../pages/NotFoundPage').then((m)=>({default:m.NotFoundPage})));
const SeatSelectionPage=lazy(()=>import('../features/booking/pages/SeatSelectionPage').then((m)=>({default:m.SeatSelectionPage})));
const CheckoutPage=lazy(()=>import('../features/booking/pages/CheckoutPage').then((m)=>({default:m.CheckoutPage})));
const BookingConfirmationPage=lazy(()=>import('../features/booking/pages/BookingConfirmationPage').then((m)=>({default:m.BookingConfirmationPage})));
const DashboardRouter=lazy(()=>import('../features/dashboard/DashboardRouter').then((m)=>({default:m.DashboardRouter})));
const CustomerDashboard=lazy(()=>import('../features/dashboard/CustomerDashboard').then((m)=>({default:m.CustomerDashboard})));
const CustomerBookingDetail=lazy(()=>import('../features/dashboard/CustomerBookingDetail').then((m)=>({default:m.CustomerBookingDetail})));
const OrganizerDashboard=lazy(()=>import('../features/dashboard/OrganizerDashboard').then((m)=>({default:m.OrganizerDashboard})));
const AdminDashboard=lazy(()=>import('../features/dashboard/AdminDashboard').then((m)=>({default:m.AdminDashboard})));

function page(element:ReactNode){return <Suspense fallback={<div className="section-wrap" style={{paddingBlock:64}}><LoadingState label="Loading page"/></div>}>{element}</Suspense>;}

export function App() {
  return <Routes><Route element={<AppShell />}>
    <Route index element={page(<HomePage />)} />
    <Route path="events" element={page(<EventsPage />)} />
    <Route path="events/:eventId" element={page(<EventDetailPage />)} />
    <Route path="login" element={page(<LoginPage />)} />
    <Route path="register" element={page(<RegisterPage />)} />
    <Route element={<ProtectedRoute />}><Route path="profile" element={page(<ProfilePage />)} /></Route>
    <Route element={<ProtectedRoute />}><Route path="dashboard" element={page(<DashboardRouter />)} /></Route>
    <Route element={<ProtectedRoute roles={['CUSTOMER']} />}>
      <Route path="dashboard/customer" element={page(<CustomerDashboard />)} />
      <Route path="dashboard/customer/bookings/:bookingId" element={page(<CustomerBookingDetail />)} />
      <Route path="events/:eventId/seats" element={page(<SeatSelectionPage />)} />
      <Route path="checkout/:bookingId" element={page(<CheckoutPage />)} />
      <Route path="bookings/:bookingId/confirmation" element={page(<BookingConfirmationPage />)} />
    </Route>
    <Route element={<ProtectedRoute roles={['ORGANIZER']} />}><Route path="dashboard/organizer" element={page(<OrganizerDashboard />)} /></Route>
    <Route element={<ProtectedRoute roles={['ADMIN']} />}><Route path="dashboard/admin" element={page(<AdminDashboard />)} /></Route>
    <Route path="404" element={page(<NotFoundPage />)} />
    <Route path="*" element={<Navigate to="/404" replace />} />
  </Route></Routes>;
}
