import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { useSearchParams } from 'react-router-dom';
import { EventFilters } from '../components/EventFilters';
import { EventCard } from '../components/EventCard';
import { useEvents } from '../hooks/useEvents';
import type { EventFilters as FilterValues } from '../../../api/events';
import { ErrorState, EventSkeletons, EmptyState } from '../../../components/ui/States';
import { getApiError } from '../../../api/http';
import { Pagination } from '../../../components/ui/Pagination';

const defaults:FilterValues={city:'',type:'',from:'',to:'',venueId:'',status:'BOOKING_OPEN',minPrice:'',maxPrice:'',sort:'startTime,asc',page:0,size:9};
function readParams(params:URLSearchParams):FilterValues {const out={...defaults};for(const key of Object.keys(defaults) as (keyof FilterValues)[]){const val=params.get(key);if(val!==null){if(key==='page'||key==='size')out[key]=Number(val);else (out as Record<string,unknown>)[key]=val;}}return out;}
export function EventsPage(){
  const [params,setParams]=useSearchParams();const applied=useMemo(()=>readParams(params),[params]);const [draft,setDraft]=useState(applied);
  useEffect(()=>setDraft(applied),[applied]);
  const query=useEvents(applied);
  const apply=(event:FormEvent)=>{event.preventDefault();const next=new URLSearchParams();for(const [key,value] of Object.entries({...draft,page:0,size:9})){if(value!==''&&value!==undefined)next.set(key,String(value));}setParams(next);};
  const reset=()=>{setDraft(defaults);const next=new URLSearchParams();next.set('status','BOOKING_OPEN');next.set('sort','startTime,asc');next.set('page','0');next.set('size','9');setParams(next);};
  const page=(number:number)=>{const next=new URLSearchParams(params);next.set('page',String(number));setParams(next);window.scrollTo({top:0,behavior:'smooth'});};
  return <div className="section-wrap listing-page"><div className="page-intro"><p className="eyebrow">Find your next moment</p><h1>Explore <em>events.</em></h1><p>Good music, big plays, front-row laughs — it’s all happening somewhere.</p></div>
    <EventFilters value={draft} onChange={setDraft} onApply={apply} onReset={reset}/>
    <div className="results-heading"><div><span className="eyebrow">Your shortlist</span><h2>{query.data?`${query.data.totalElements} events`: 'Events for you'}</h2></div><span className="results-page">Page {(applied.page||0)+1}</span></div>
    {query.isPending?<EventSkeletons/>:query.isError?<ErrorState message={getApiError(query.error)} retry={()=>void query.refetch()}/>:query.data?.content.length?<><div className="event-grid">{query.data.content.map((event,index)=><EventCard key={event.id} event={event} index={index}/>)}</div><Pagination page={query.data.number} totalPages={query.data.totalPages} onChange={page}/></>:<EmptyState title="No events match just yet" description="Try a different city, date, or price range to find something great."/>}
  </div>;
}
