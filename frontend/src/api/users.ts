import type { UserProfile } from '../types/api';
import { http } from './http';
export async function getMyProfile() {
  const { data } = await http.get<UserProfile>('/user/me');
  return data;
}
export async function updateMyProfile(name: string) {
  const { data } = await http.put<UserProfile>('/user/me', { name });
  return data;
}
export async function getAdminUsers() { const { data } = await http.get<UserProfile[]>('/user/admin/users'); return data; }
export async function updateUserAccount(id: number, role: UserProfile['role'], active: boolean) { const { data } = await http.put<UserProfile>(`/user/admin/users/${id}`, { role, active }); return data; }
