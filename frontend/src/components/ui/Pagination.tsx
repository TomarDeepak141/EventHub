import { ArrowLeft, ArrowRight } from 'lucide-react';
import { Button } from './Button';
export function Pagination({page,totalPages,onChange}:{page:number;totalPages:number;onChange:(page:number)=>void}){
  if(totalPages<=1)return null;
  return <nav className="pagination" aria-label="Event pages"><Button variant="secondary" disabled={page<=0} onClick={()=>onChange(page-1)}><ArrowLeft size={15}/> Previous</Button><span>Page <b>{page+1}</b> of {totalPages}</span><Button variant="secondary" disabled={page+1>=totalPages} onClick={()=>onChange(page+1)}>Next <ArrowRight size={15}/></Button></nav>;
}
