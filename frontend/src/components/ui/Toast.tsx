import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';
import { Check, CircleAlert, X } from 'lucide-react';
import type { ReactElement } from 'react';

type ToastKind='success'|'error'|'info';
interface Toast { id: number; title: string; kind: ToastKind }
interface ToastContextValue { showToast: (title: string, kind?: ToastKind) => void }
const ToastContext=createContext<ToastContextValue|null>(null);
export function ToastProvider({ children }: { children: ReactNode }) {
  const [items,setItems]=useState<Toast[]>([]);
  const showToast=useCallback((title:string,kind:ToastKind='success')=>{
    const id=Date.now()+Math.random();setItems((all)=>[...all,{id,title,kind}]);window.setTimeout(()=>setItems((all)=>all.filter((item)=>item.id!==id)),4000);
  },[]);
  const value=useMemo(()=>({showToast}),[showToast]);
  return <ToastContext.Provider value={value}>{children}<div className="toast-stack" aria-live="polite">{items.map((toast)=><ToastItem key={toast.id} toast={toast} dismiss={()=>setItems((all)=>all.filter((x)=>x.id!==toast.id))} />)}</div></ToastContext.Provider>;
}
function ToastItem({toast,dismiss}:{toast:Toast;dismiss:()=>void}) {
  const icon:Record<ToastKind,ReactElement>={success:<Check size={17}/>,error:<CircleAlert size={17}/>,info:<CircleAlert size={17}/>};
  return <div className={`toast toast-${toast.kind}`} role="status">{icon[toast.kind]}<span>{toast.title}</span><button aria-label="Dismiss notification" onClick={dismiss}><X size={15}/></button></div>;
}
export function useToast(){const value=useContext(ToastContext);if(!value)throw new Error('useToast must be used within ToastProvider');return value;}
