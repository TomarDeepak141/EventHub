import { ArrowDownRight, ArrowRight, ArrowUpRight, Music2, Theater, Trophy, Clapperboard, Mic2, Sparkles } from 'lucide-react';
import { Link } from 'react-router-dom';
import type { LucideIcon } from 'lucide-react';
import { EventCard } from '../components/EventCard';
import { CATEGORIES } from '../constants';
import { useFeaturedEvents } from '../hooks/useEvents';
import { EventSkeletons, ErrorState, EmptyState } from '../../../components/ui/States';
import { getApiError } from '../../../api/http';

const categoryIcons:Partial<Record<(typeof CATEGORIES)[number],LucideIcon>>={CONCERT:Music2,THEATRE:Theater,MOVIE:Clapperboard,SPORTS:Trophy,STANDUP:Mic2,WORKSHOP:Sparkles};
export function HomePage(){
  const featured=useFeaturedEvents();
  return <>
    <section className="hero"><div className="hero-inner"><div className="hero-copy"><p className="eyebrow hero-eyebrow"><span className="eyebrow-dot"/> A little more life, out there</p><h1>Make plans<br/>worth <em>remembering.</em></h1><p className="hero-description">The show you’ve been meaning to see. The game you’ll talk about all week. Find your next favorite moment right here.</p><div className="hero-actions"><Link className="button button-primary" to="/events">Explore events <ArrowRight size={17}/></Link><a href="#featured" className="hero-text-link">See what’s on <ArrowDownRight size={16}/></a></div><div className="hero-proof"><div className="proof-stack"><span>♫</span><span>✦</span><span>↗</span></div><span>Good nights start with a plan</span><span className="proof-star">✳</span></div></div>
      <div className="hero-visual" aria-label="Colorful abstract event poster artwork"><div className="hero-grid"/><div className="hero-ring ring-one"/><div className="hero-ring ring-two"/><div className="hero-sun"/><div className="hero-poster"><span className="poster-kicker">MAKE SOME NOISE</span><span className="poster-note">♫</span><span className="poster-name">LIVE<br/><i>LOUD.</i></span><span className="poster-date">YOUR NIGHT IS WAITING</span><span className="poster-sticker">good<br/>things<br/><b>happen</b></span></div><div className="floating-note note-a">TONIGHT ↗</div><div className="floating-note note-b">✳ GO OUTSIDE ✳</div></div>
    </div><a href="#categories" className="hero-scroll">Scroll to explore <ArrowDownRight size={15}/></a></section>
    <section id="categories" className="section-wrap category-section"><div className="section-heading"><div><p className="eyebrow">Pick your mood</p><h2>What are you <em>into?</em></h2></div><Link to="/events" className="quiet-link">All events <ArrowUpRight size={16}/></Link></div><div className="category-grid">{CATEGORIES.map((type,index)=>{const Icon=categoryIcons[type]??Sparkles;return <Link key={type} to={`/events?type=${type}`} className={`category-tile category-tile-${index}`}><span className="category-symbol"><Icon size={21}/></span><span>{type[0]+type.slice(1).toLowerCase()}</span><ArrowUpRight size={16}/></Link>;})}</div></section>
    <section className="section-wrap featured-section" id="featured"><div className="section-heading"><div><p className="eyebrow">The good stuff</p><h2>Coming up <em>soon.</em></h2><p className="section-subtitle">A few things we think you’ll love.</p></div><Link to="/events" className="button button-secondary">Browse everything <ArrowRight size={16}/></Link></div>
      {featured.isLoading?<EventSkeletons/>:featured.isError?<ErrorState message={getApiError(featured.error)} retry={()=>void featured.refetch()}/>:featured.data?.content.length?<div className="event-grid">{featured.data.content.slice(0,3).map((event,index)=><EventCard key={event.id} event={event} index={index}/>)}</div>:<EmptyState title="The calendar is taking a breather" description="Check back soon or browse all events."/>}
    </section>
    <section className="closing-banner"><div className="closing-mark">✳</div><div><p className="eyebrow">Your next story starts here</p><h2>Same old plans?<br/><em>Let’s change that.</em></h2></div><Link className="button button-light" to="/events">Find your event <ArrowUpRight size={17}/></Link></section>
  </>;
}
