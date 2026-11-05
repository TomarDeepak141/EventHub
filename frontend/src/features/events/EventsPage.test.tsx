import { QueryClient,QueryClientProvider } from '@tanstack/react-query';
import { render,screen } from '@testing-library/react';
import { MemoryRouter,Route,Routes } from 'react-router-dom';
import { afterEach,describe,expect,it,vi } from 'vitest';
import { getEvents } from '../../api/events';
import { EventsPage } from './pages/EventsPage';
import type { Page } from '../../types/api';

vi.mock('../../api/events',()=>({getEvents:vi.fn(),getEvent:vi.fn(),getAvailability:vi.fn()}));
const mockedGetEvents=vi.mocked(getEvents);
function renderPage(){
  const client=new QueryClient({defaultOptions:{queries:{retry:false,gcTime:0}}});
  return render(<QueryClientProvider client={client}><MemoryRouter initialEntries={['/events']}><Routes><Route path="/events" element={<EventsPage/>}/></Routes></MemoryRouter></QueryClientProvider>);
}
const emptyPage:Page<never>={content:[],totalElements:0,totalPages:0,number:0,size:9,first:true,last:true,empty:true};
afterEach(()=>vi.clearAllMocks());

describe('event discovery states',()=>{
  it('shows loading feedback while the event request is pending',()=>{
    mockedGetEvents.mockReturnValue(new Promise(()=>{}));renderPage();
    expect(screen.getByRole('status',{name:'Loading events'})).toBeInTheDocument();
  });
  it('shows a useful error when event discovery fails',async()=>{
    mockedGetEvents.mockRejectedValue(new Error('service offline'));renderPage();
    expect(await screen.findByText('service offline')).toBeInTheDocument();
  });
  it('shows an empty state when no events match',async()=>{
    mockedGetEvents.mockResolvedValue(emptyPage);renderPage();
    expect(await screen.findByText('No events match just yet')).toBeInTheDocument();
  });
});
