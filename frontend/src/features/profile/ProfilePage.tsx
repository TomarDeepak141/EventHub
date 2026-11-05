import { useEffect } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ArrowRight, BadgeCheck, Mail, ShieldCheck, UserRound } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { getMyProfile, updateMyProfile } from '../../api/users';
import { getApiError } from '../../api/http';
import { useAuth } from '../auth/AuthProvider';
import { profileSchema, type ProfileValues } from '../auth/schemas';
import { Button } from '../../components/ui/Button';
import { TextField } from '../../components/ui/TextField';
import { LoadingState, ErrorState } from '../../components/ui/States';
import { useToast } from '../../components/ui/Toast';
import { initials } from '../../utils/format';

export function ProfilePage(){
  const query=useQuery({queryKey:['profile'],queryFn:getMyProfile});const client=useQueryClient();const {updateUser}=useAuth();const toast=useToast();
  const {register,handleSubmit,reset,formState:{errors}}=useForm<ProfileValues>({resolver:zodResolver(profileSchema),defaultValues:{name:''}});
  useEffect(()=>{if(query.data)reset({name:query.data.name});},[query.data,reset]);
  const mutation=useMutation({mutationFn:(name:string)=>updateMyProfile(name),onSuccess:(profile)=>{client.setQueryData(['profile'],profile);updateUser(profile);toast.showToast('Profile updated.');},onError:(error)=>toast.showToast(getApiError(error),'error')});
  if(query.isPending)return <div className="section-wrap profile-wrap"><LoadingState label="Loading profile"/></div>;
  if(query.isError||!query.data)return <div className="section-wrap profile-wrap"><ErrorState message={query.error?getApiError(query.error):'Profile unavailable'} retry={()=>void query.refetch()}/></div>;
  const profile=query.data;
  return <div className="section-wrap profile-wrap"><div className="page-intro"><p className="eyebrow">Your EventHub</p><h1>Your <em>profile.</em></h1><p>Keep your details up to date and you’re all set for what’s next.</p></div><section className="profile-card"><div className="profile-head"><div className="avatar-large">{initials(profile.name)}</div><div><span className="eyebrow">Member profile</span><h2>{profile.name}</h2><p><Mail size={15}/>{profile.email}</p></div><span className="profile-role"><BadgeCheck size={15}/>{profile.role}</span></div><div className="profile-divider"/><form className="profile-form" onSubmit={handleSubmit((values)=>mutation.mutate(values.name))}><div><h3>Personal details</h3><p>Update the name associated with your account.</p></div><TextField label="Display name" autoComplete="name" error={errors.name?.message} {...register('name')}/><div className="profile-locked"><span className="locked-icon"><ShieldCheck size={17}/></span><div><strong>Account email</strong><span>{profile.email}</span></div><span className="locked-note">Managed securely</span></div><Button type="submit" loading={mutation.isPending}>Save changes <ArrowRight size={16}/></Button></form><div className="profile-footer"><UserRound size={15}/> Your account is active and protected by EventHub.</div></section></div>;
}
