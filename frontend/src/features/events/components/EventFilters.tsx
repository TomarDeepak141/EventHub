import { Filter, MapPin, RotateCcw, Search } from 'lucide-react';
import type { FormEvent } from 'react';
import type { EventFilters as FilterValues } from '../../../api/events';
import { EVENT_TYPES, EVENT_STATUSES } from '../constants';
import { Button } from '../../../components/ui/Button';

export function EventFilters({value,onChange,onApply,onReset}:{value:FilterValues;onChange:(next:FilterValues)=>void;onApply:(event:FormEvent)=>void;onReset:()=>void}){
  const set=(key:keyof FilterValues,val:string)=>onChange({...value,[key]:val,page:0});
  return <form className="filter-panel" onSubmit={onApply}><div className="filter-heading"><div><span className="eyebrow"><Filter size={13}/> Refine your search</span><h2>Find your kind of night</h2></div><button type="button" className="reset-button" onClick={onReset}><RotateCcw size={14}/> Reset</button></div>
    <div className="filter-grid">
      <label className="filter-field"><span>City</span><div className="input-icon"><MapPinIcon/><input value={value.city||''} onChange={(e)=>set('city',e.target.value)} placeholder="Any city" /></div></label>
      <label className="filter-field"><span>Event type</span><select value={value.type||''} onChange={(e)=>set('type',e.target.value)}><option value="">All experiences</option>{EVENT_TYPES.map((x)=><option key={x} value={x}>{x[0]+x.slice(1).toLowerCase()}</option>)}</select></label>
      <label className="filter-field"><span>From</span><input type="datetime-local" value={value.from||''} onChange={(e)=>set('from',e.target.value)}/></label>
      <label className="filter-field"><span>To</span><input type="datetime-local" value={value.to||''} onChange={(e)=>set('to',e.target.value)}/></label>
      <label className="filter-field"><span>Venue ID</span><input type="number" min="1" value={value.venueId||''} onChange={(e)=>set('venueId',e.target.value)} placeholder="Any venue"/></label>
      <label className="filter-field"><span>Availability</span><select value={value.status||'BOOKING_OPEN'} onChange={(e)=>set('status',e.target.value)}><option value="">Any status</option>{EVENT_STATUSES.map((x)=><option key={x} value={x}>{x.replaceAll('_',' ')}</option>)}</select></label>
      <label className="filter-field"><span>Min ticket price</span><input type="number" min="0" step="0.01" value={value.minPrice||''} onChange={(e)=>set('minPrice',e.target.value)} placeholder="0.00"/></label>
      <label className="filter-field"><span>Max ticket price</span><input type="number" min="0" step="0.01" value={value.maxPrice||''} onChange={(e)=>set('maxPrice',e.target.value)} placeholder="No limit"/></label>
      <label className="filter-field"><span>Sort by</span><select value={value.sort||'startTime,asc'} onChange={(e)=>set('sort',e.target.value)}><option value="startTime,asc">Soonest first</option><option value="startTime,desc">Latest first</option><option value="name,asc">Name A–Z</option><option value="createdAt,desc">Recently added</option></select></label>
    </div><div className="filter-actions"><p>Price ranges use the venue’s seat category pricing.</p><Button type="submit"><Search size={16}/> Show events</Button></div>
  </form>;
}
function MapPinIcon(){return <MapPin className="input-prefix" size={15}/>}
