import { ArrowUpRight, CalendarDays, MapPin } from 'lucide-react';
import { Link } from 'react-router-dom';
import type { EventItem } from '../../../types/api';
import { formatEventDate } from '../../../utils/format';

const accents=['event-art-teal','event-art-orange','event-art-lilac','event-art-blue'];
const glyphs:Record<string,string>={CONCERT:'♫',MOVIE:'✦',SPORTS:'↗',THEATRE:'◌',STANDUP:'◡',SEMINAR:'✳',WORKSHOP:'✴',OTHER:'✧'};
export function EventCard({event,index=0}:{event:EventItem;index?:number}){
  return <article className="event-card">
    <Link to={`/events/${event.id}`} className={`event-art ${accents[index%accents.length]}`} aria-label={`View ${event.name}`}>
      {event.imageUrl?<img src={event.imageUrl} alt="" loading="lazy"/>:<><span className="art-orb"/><span className="art-glyph">{glyphs[event.eventType]||'✧'}</span><span className="art-lines"/></>}
      <span className="event-art-label">{event.eventType}</span><span className="art-arrow"><ArrowUpRight size={18}/></span>
    </Link>
    <div className="event-card-body"><div className="event-card-meta"><span><CalendarDays size={14}/>{formatEventDate(event.startTime)}</span><span className={`status-chip status-${event.eventStatus.toLowerCase()}`}>{event.eventStatus.replaceAll('_',' ')}</span></div>
      <Link className="event-title-link" to={`/events/${event.id}`}><h3>{event.name}</h3></Link>
      <p className="event-location"><MapPin size={15}/>{event.venueName} · {event.city}</p>
      <div className="event-card-bottom"><span>{event.screenName}</span><Link to={`/events/${event.id}`} aria-label={`Details for ${event.name}`}>Details <ArrowUpRight size={15}/></Link></div>
    </div>
  </article>;
}
