import { render,screen } from '@testing-library/react';
import { MemoryRouter,Route,Routes } from 'react-router-dom';
import { describe,expect,it } from 'vitest';
import { AuthProvider } from './AuthProvider';
import { ProtectedRoute } from './ProtectedRoute';
import { beforeEach } from 'vitest';

beforeEach(()=>localStorage.clear());

describe('ProtectedRoute',()=>{
  it('redirects signed-out visitors to sign in and remembers the requested path',()=>{
    render(<MemoryRouter initialEntries={['/profile']}><AuthProvider><Routes><Route element={<ProtectedRoute/>}><Route path="/profile" element={<h1>Private profile</h1>}/></Route><Route path="/login" element={<h1>Sign in screen</h1>}/></Routes></AuthProvider></MemoryRouter>);
    expect(screen.getByRole('heading',{name:'Sign in screen'})).toBeInTheDocument();
    expect(screen.queryByRole('heading',{name:'Private profile'})).not.toBeInTheDocument();
  });
  it('shows a clear unauthorized state when a signed-in role opens another dashboard',()=>{
    localStorage.setItem('eventhub.session',JSON.stringify({token:'test-token',user:{id:1,email:'customer@test',role:'CUSTOMER'}}));
    render(<MemoryRouter initialEntries={['/dashboard/admin']}><AuthProvider><Routes><Route element={<ProtectedRoute roles={['ADMIN']}/>}><Route path="/dashboard/admin" element={<h1>Admin controls</h1>}/></Route><Route path="/dashboard" element={<h1>Your dashboard</h1>}/></Routes></AuthProvider></MemoryRouter>);
    expect(screen.getByRole('alert')).toHaveTextContent(/not available to your role/i);
    expect(screen.queryByRole('heading',{name:'Admin controls'})).not.toBeInTheDocument();
  });
});
