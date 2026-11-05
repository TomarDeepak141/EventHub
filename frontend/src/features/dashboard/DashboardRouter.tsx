import { Navigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';
export function DashboardRouter() { const { user } = useAuth(); if (user?.role === 'CUSTOMER') return <Navigate to="/dashboard/customer" replace/>; if (user?.role === 'ORGANIZER') return <Navigate to="/dashboard/organizer" replace/>; if (user?.role === 'ADMIN') return <Navigate to="/dashboard/admin" replace/>; return <Navigate to="/login" replace/>; }
