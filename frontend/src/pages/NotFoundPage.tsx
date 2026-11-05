import { Link } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
export function NotFoundPage(){return <div className="not-found"><span className="not-found-number">404</span><p className="eyebrow">That’s a plot twist</p><h1>This page wandered<br/><em>off the guest list.</em></h1><Link className="button button-primary" to="/"><ArrowLeft size={16}/> Back to EventHub</Link></div>;}
