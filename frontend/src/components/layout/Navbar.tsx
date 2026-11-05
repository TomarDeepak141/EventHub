import { useState } from 'react';
import { Link, NavLink } from 'react-router-dom';
import { ArrowUpRight, Compass, LayoutDashboard, Menu, Ticket, UserRound, X } from 'lucide-react';
import { useAuth } from '../../features/auth/AuthProvider';
import { Button } from '../ui/Button';

export function Navbar() {
  const {user,logout}=useAuth();const [open,setOpen]=useState(false);
  const close=()=>setOpen(false);
  return <header className="site-header"><div className="nav-shell">
    <Link to="/" className="brand" onClick={close}><span className="brand-mark"><Ticket size={18}/></span><span>event<span className="brand-light">hub</span></span><span className="brand-dot">.</span></Link>
    <button className="mobile-menu" aria-label={open?'Close menu':'Open menu'} onClick={()=>setOpen(!open)}>{open?<X/>:<Menu/>}</button>
    <nav className={`nav-links ${open?'nav-open':''}`} aria-label="Main navigation">
      <NavLink to="/events" onClick={close} className={({isActive})=>isActive?'nav-link active':'nav-link'}><Compass size={16}/>Explore</NavLink>
      {user&&<span className={`role-pill ${user.role.toLowerCase()}-pill`}>{user.role[0]+user.role.slice(1).toLowerCase()}</span>}
      {user?<><NavLink to="/dashboard" onClick={close} className={({isActive})=>isActive?'nav-link active':'nav-link'}><LayoutDashboard size={16}/>Dashboard</NavLink><NavLink to="/profile" onClick={close} className={({isActive})=>isActive?'nav-link active':'nav-link'}><UserRound size={16}/>Profile</NavLink><button className="nav-link nav-logout" onClick={()=>{logout();close();}}>Sign out</button></>
        :<><NavLink to="/login" onClick={close} className="nav-link">Sign in</NavLink><Link to="/register" onClick={close}><Button className="nav-cta">Create account <ArrowUpRight size={15}/></Button></Link></>}
    </nav>
  </div></header>;
}
