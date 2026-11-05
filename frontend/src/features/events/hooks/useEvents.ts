import { useQuery } from '@tanstack/react-query';
import { getAvailability, getEvent, getEvents, type EventFilters } from '../../../api/events';
export function useEvents(filters:EventFilters){return useQuery({queryKey:['events',filters],queryFn:()=>getEvents(filters)});}
export function useFeaturedEvents(){return useQuery({queryKey:['events','featured'],queryFn:()=>getEvents({size:4,sort:'startTime,asc',status:'BOOKING_OPEN'})});}
export function useEvent(id:number){return useQuery({queryKey:['event',id],queryFn:()=>getEvent(id),enabled:Number.isFinite(id)});}
export function useEventAvailability(id:number){return useQuery({queryKey:['event',id,'availability'],queryFn:()=>getAvailability(id),enabled:Number.isFinite(id),refetchInterval:30_000});}
