import { AlertTriangle, CalendarDays, LoaderCircle } from 'lucide-react';
export function LoadingState({ label='Loading events' }: { label?: string }) {
  return <div role="status" aria-label={label} className="state-panel"><LoaderCircle className="spin text-teal" size={28} /><span className="sr-only">{label}</span><div className="skeleton-lines"><i /><i /><i /></div></div>;
}
export function EventSkeletons() { return <div role="status" className="event-grid" aria-label="Loading events">{[0,1,2,3].map((n)=><div className="event-skeleton" key={n}><i /><b /><span /><span /></div>)}</div>; }
export function EmptyState({ title='Nothing here yet', description='Try adjusting your filters.' }: { title?: string; description?: string }) {
  return <div className="state-panel"><span className="state-icon"><CalendarDays size={22} /></span><h3>{title}</h3><p>{description}</p></div>;
}
export function ErrorState({ message, retry }: { message: string; retry?: () => void }) {
  return <div className="state-panel error-panel"><span className="state-icon"><AlertTriangle size={22} /></span><h3>We hit a snag</h3><p>{message}</p>{retry && <button className="text-button" onClick={retry}>Try again</button>}</div>;
}
