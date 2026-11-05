import type { LoginResponse, UserProfile } from '../types/api';
import { http } from './http';

export interface RegisterRequest { name: string; email: string; password: string }
export interface LoginRequest { email: string; password: string }
export async function login(request: LoginRequest) {
  const { data } = await http.post<LoginResponse>('/auth/login', request);
  return data;
}
export async function register(request: RegisterRequest) {
  const { data } = await http.post<UserProfile>('/user/register', request);
  return data;
}
