import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { updateUserAccount } from '../../api/users';
import { AccountManagementPanel } from './AccountManagementPanel';

vi.mock('../../api/users', () => ({ updateUserAccount: vi.fn() }));

const accounts = [
  { id: 1, name: 'Customer One', email: 'customer@example.test', role: 'CUSTOMER' as const, active: true },
  { id: 2, name: 'Platform Admin', email: 'admin@example.test', role: 'ADMIN' as const, active: true },
];
function renderPanel() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } });
  return render(<QueryClientProvider client={client}><AccountManagementPanel users={accounts} currentUserId={2}/></QueryClientProvider>);
}
afterEach(() => vi.restoreAllMocks());

describe('admin account role changes', () => {
  it('confirms and submits a customer-to-organizer change with success feedback', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    vi.mocked(updateUserAccount).mockResolvedValue({ ...accounts[0], role: 'ORGANIZER' } as never);
    renderPanel();

    const role = screen.getByRole('combobox', { name: 'Role for customer@example.test' });
    expect([...role.querySelectorAll('option')].map((option) => option.value)).toEqual(['CUSTOMER', 'ORGANIZER']);
    fireEvent.change(role, { target: { value: 'ORGANIZER' } });
    fireEvent.click(screen.getByRole('button', { name: 'Save changes for customer@example.test' }));

    expect(window.confirm).toHaveBeenCalledWith(expect.stringContaining('change role from CUSTOMER to ORGANIZER'));
    await waitFor(() => expect(updateUserAccount).toHaveBeenCalledWith(1, 'ORGANIZER', true));
    expect(await screen.findByRole('status')).toHaveTextContent('customer@example.test updated.');
  });

  it('locks admin role changes and the signed-in administrator account', () => {
    renderPanel();
    expect(screen.getByRole('combobox', { name: 'Role for admin@example.test' })).toBeDisabled();
    expect(screen.getByRole('checkbox', { name: 'Active for admin@example.test' })).toBeDisabled();
    expect(screen.getByRole('combobox', { name: 'Role for customer@example.test' }).querySelector('option[value="ADMIN"]')).toBeNull();
  });
});
