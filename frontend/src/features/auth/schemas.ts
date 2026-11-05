import { z } from 'zod';
export const loginSchema=z.object({email:z.email('Enter a valid email address'),password:z.string().min(8,'Password must be at least 8 characters')});
export const registerSchema=z.object({name:z.string().trim().min(2,'Name must be at least 2 characters').max(120,'Name is too long'),email:z.email('Enter a valid email address'),password:z.string().min(8,'Use at least 8 characters').max(72,'Password must be 72 characters or fewer')});
export const profileSchema=z.object({name:z.string().trim().min(2,'Name must be at least 2 characters').max(120,'Name is too long')});
export type LoginValues=z.infer<typeof loginSchema>;export type RegisterValues=z.infer<typeof registerSchema>;export type ProfileValues=z.infer<typeof profileSchema>;
