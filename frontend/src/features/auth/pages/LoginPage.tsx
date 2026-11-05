import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { ArrowRight, Eye, EyeOff, LockKeyhole, Mail } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { loginSchema, type LoginValues } from '../schemas';
import { useAuth } from '../AuthProvider';
import { TextField } from '../../../components/ui/TextField';
import { Button } from '../../../components/ui/Button';
import { getApiError } from '../../../api/http';
import { useToast } from '../../../components/ui/Toast';

export function LoginPage(){
  const {login}=useAuth();const navigate=useNavigate();const location=useLocation();const toast=useToast();const [showPassword,setShowPassword]=useState(false);const [apiError,setApiError]=useState('');
  const {register,handleSubmit,formState:{errors,isSubmitting}}=useForm<LoginValues>({resolver:zodResolver(loginSchema),defaultValues:{email:'',password:''}});
  const submit=handleSubmit(async(values)=>{setApiError('');try{await login(values.email,values.password);toast.showToast('Welcome back. You’re signed in.');const from=(location.state as {from?:string}|null)?.from;navigate(from||'/profile',{replace:true});}catch(error){setApiError(getApiError(error));}});
  return <div className="auth-layout"><div className="auth-aside auth-aside-login"><div className="auth-aside-art"><span>✳</span><div className="aside-caption">GOOD NIGHTS<br/><i>START HERE</i></div></div><p className="eyebrow">Your plans, all in one place</p><h2>Make time for<br/><em>the good stuff.</em></h2><p>Come back to the events, people, and moments you love.</p></div><section className="auth-card"><span className="auth-icon"><LockKeyhole size={21}/></span><p className="eyebrow">Welcome back</p><h1>Sign in to <em>EventHub.</em></h1><p className="auth-lede">Your next favorite memory is just a few taps away.</p>
    {apiError&&<div className="form-alert" role="alert">{apiError}</div>}<form className="auth-form" onSubmit={submit} noValidate><TextField label="Email address" type="email" autoComplete="email" placeholder="you@example.com" error={errors.email?.message} {...register('email')}/><TextField label="Password" type={showPassword?'text':'password'} autoComplete="current-password" placeholder="Your password" error={errors.password?.message} {...register('password')} /><button type="button" className="password-toggle" aria-label={showPassword?'Hide password':'Show password'} onClick={()=>setShowPassword(!showPassword)}>{showPassword?<EyeOff size={16}/>:<Eye size={16}/>}</button><Button type="submit" loading={isSubmitting} className="auth-submit">Sign in <ArrowRight size={17}/></Button></form><p className="auth-switch">New around here? <Link to="/register">Create an account</Link></p><div className="auth-security"><Mail size={14}/> Your account is secured with EventHub authentication.</div>
  </section></div>;
}
