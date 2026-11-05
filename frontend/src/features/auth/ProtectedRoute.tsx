import { Link, Navigate, Outlet, useLocation } from 'react-router-dom';
import type { Role } from '../../types/api';
import { useAuth } from './AuthProvider';
export function ProtectedRoute({roles}:{roles?:Role[]}={}){
  const {user,isAuthenticated}=useAuth();const location=useLocation();
  if(!isAuthenticated)return <Navigate to="/login" replace state={{from:location.pathname}}/>;
  if(roles&&user&&!roles.includes(user.role))return <main className="section-wrap dashboard-wrap"><section className="unauthorized-state" role="alert"><p className="eyebrow">Access restricted</p><h1>This area is not available to your role.</h1><p>Your signed-in account does not have permission to open this dashboard.</p><Link className="button button-primary" to="/dashboard">Go to your dashboard</Link></section></main>;
  return <Outlet/>;
}
