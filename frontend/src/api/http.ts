import axios from 'axios';
import type { ApiErrorBody } from '../types/api';

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15_000,
});

http.interceptors.response.use((response)=>response,(error:unknown)=>{
  if(axios.isAxiosError(error)&&error.response?.status===401){
    localStorage.removeItem('eventhub.session');
    window.dispatchEvent(new Event('eventhub:unauthorized'));
  }
  return Promise.reject(error);
});

http.interceptors.request.use((config) => {
  const raw = localStorage.getItem('eventhub.session');
  if (raw) {
    try {
      const token = (JSON.parse(raw) as { token?: string }).token;
      if (token) config.headers.Authorization = `Bearer ${token}`;
    } catch { localStorage.removeItem('eventhub.session'); }
  }
  return config;
});

export function getApiError(error: unknown): string {
  if (axios.isAxiosError<ApiErrorBody>(error)) {
    const body = error.response?.data;
    const validation = body?.errors ? Object.values(body.errors)[0] : undefined;
    return validation || body?.message || error.message || 'The request could not be completed.';
  }
  return error instanceof Error ? error.message : 'Something unexpected happened.';
}
