import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import * as authApi from '../../api/auth';
import type { Role, UserProfile } from '../../types/api';

interface AuthUser { id: number; email: string; role: Role; name?: string; active?: boolean }
interface Session { token: string; user: AuthUser }
interface AuthContextValue {
  user: AuthUser | null; isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>; register: typeof authApi.register;
  logout: () => void; updateUser: (profile: UserProfile) => void;
}
const KEY = 'eventhub.session';
function restore(): Session | null {
  try { const value = localStorage.getItem(KEY); return value ? JSON.parse(value) as Session : null; }
  catch { localStorage.removeItem(KEY); return null; }
}
const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(restore);
  useEffect(()=>{const clear=()=>setSession(null);window.addEventListener('eventhub:unauthorized',clear);return()=>window.removeEventListener('eventhub:unauthorized',clear);},[]);
  const login = useCallback(async (email: string, password: string) => {
    const result = await authApi.login({ email, password });
    const next = { token: result.jwt, user: { id: result.userId, email: result.email, role: result.role } };
    localStorage.setItem(KEY, JSON.stringify(next)); setSession(next);
  }, []);
  const logout = useCallback(() => { localStorage.removeItem(KEY); setSession(null); }, []);
  const updateUser = useCallback((profile: UserProfile) => {
    setSession((current) => {
      if (!current) return current;
      const next = { ...current, user: { ...current.user, ...profile } };
      localStorage.setItem(KEY, JSON.stringify(next)); return next;
    });
  }, []);
  const value = useMemo(() => ({ user: session?.user ?? null, isAuthenticated: Boolean(session?.token), login, register: authApi.register, logout, updateUser }), [session, login, logout, updateUser]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error('useAuth must be used within AuthProvider');
  return value;
}
