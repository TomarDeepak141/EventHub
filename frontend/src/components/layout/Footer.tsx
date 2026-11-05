import { Link } from 'react-router-dom';
import { ArrowUpRight, Ticket } from 'lucide-react';
export function Footer(){return <footer className="site-footer"><div className="footer-inner"><Link to="/" className="brand footer-brand"><span className="brand-mark"><Ticket size={17}/></span><span>event<span className="brand-light">hub</span></span><span className="brand-dot">.</span></Link><p>Make room for a little more living.</p><Link to="/events" className="footer-link">Find an event <ArrowUpRight size={15}/></Link><span className="footer-copy">© 2026 EventHub</span></div></footer>;}
