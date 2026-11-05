import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ArrowRight, Eye, EyeOff, UserRoundPlus } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { registerSchema, type RegisterValues } from '../schemas';
import { useAuth } from '../AuthProvider';
import { TextField } from '../../../components/ui/TextField';
import { Button } from '../../../components/ui/Button';
import { getApiError } from '../../../api/http';
import { useToast } from '../../../components/ui/Toast';

export function RegisterPage(){
  const {register:registerAccount}=useAuth();const navigate=useNavigate();const toast=useToast();const [showPassword,setShowPassword]=useState(false);const [apiError,setApiError]=useState('');
  const {register,handleSubmit,formState:{errors,isSubmitting}}=useForm<RegisterValues>({resolver:zodResolver(registerSchema),defaultValues:{name:'',email:'',password:''}});
  const submit=handleSubmit(async(values)=>{setApiError('');try{await registerAccount(values);toast.showToast('Your account is ready. Sign in to continue.');navigate('/login',{replace:true,state:{registered:true}});}catch(error){setApiError(getApiError(error));}});
  return <div className="auth-layout"><div className="auth-aside auth-aside-register"><div className="register-orbit"><span>✦</span><span>♫</span><span>↗</span></div><p className="eyebrow">A world of plans</p><h2>Life’s better<br/><em>when it’s live.</em></h2><p>Discover the shows, games, and little adventures that make a week.</p></div><section className="auth-card"><span className="auth-icon"><UserRoundPlus size={21}/></span><p className="eyebrow">Get started</p><h1>Make an <em>entrance.</em></h1><p className="auth-lede">Create your free customer account and find your next event.</p>
    {apiError&&<div className="form-alert" role="alert">{apiError}</div>}<form className="auth-form" onSubmit={submit} noValidate><TextField label="Your name" autoComplete="name" placeholder="First and last name" error={errors.name?.message} {...register('name')}/><TextField label="Email address" type="email" autoComplete="email" placeholder="you@example.com" error={errors.email?.message} {...register('email')}/><TextField label="Password" type={showPassword?'text':'password'} autoComplete="new-password" placeholder="At least 8 characters" error={errors.password?.message} {...register('password')}/><button type="button" className="password-toggle register-toggle" aria-label={showPassword?'Hide password':'Show password'} onClick={()=>setShowPassword(!showPassword)}>{showPassword?<EyeOff size={16}/>:<Eye size={16}/>}</button><Button type="submit" loading={isSubmitting} className="auth-submit">Create account <ArrowRight size={17}/></Button></form><p className="auth-switch">Already a member? <Link to="/login">Sign in</Link></p><p className="auth-terms">By joining, you agree to keep things kind and have a good time.</p>
  </section></div>;
}
