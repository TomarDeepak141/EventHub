import { render,screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { expect,it } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { AuthProvider } from '../AuthProvider';
import { ToastProvider } from '../../../../src/components/ui/Toast';
import { LoginPage } from './LoginPage';

it('shows accessible validation messages for an incomplete login form',async()=>{
  const user=userEvent.setup();
  render(<MemoryRouter><AuthProvider><ToastProvider><LoginPage/></ToastProvider></AuthProvider></MemoryRouter>);
  await user.click(screen.getByRole('button',{name:/sign in/i}));
  expect(await screen.findByText('Enter a valid email address')).toBeInTheDocument();
  expect(screen.getByText('Password must be at least 8 characters')).toBeInTheDocument();
});
