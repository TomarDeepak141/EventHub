import { useEffect, useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { getApiError } from '../../api/http';
import { updateUserAccount } from '../../api/users';
import { EmptyState } from '../../components/ui/States';
import type { Role, UserProfile } from '../../types/api';

interface Props {
  users: UserProfile[];
  currentUserId?: number;
}

type Draft = { role: Role; active: boolean };

export function AccountManagementPanel({ users, currentUserId }: Props) {
  const client = useQueryClient();
  const [drafts, setDrafts] = useState<Record<number, Draft>>({});
  const [message, setMessage] = useState('');
  const mutation = useMutation({
    mutationFn: ({ id, draft }: { id: number; draft: Draft }) => updateUserAccount(id, draft.role, draft.active),
    onSuccess: async (updated) => {
      setMessage(`${updated.email} updated.`);
      await client.invalidateQueries({ queryKey: ['admin', 'users'] });
    },
  });

  useEffect(() => {
    setDrafts(Object.fromEntries(users.map((account) => [account.id, { role: account.role, active: account.active }])));
  }, [users]);

  const save = (account: UserProfile, draft: Draft) => {
    const changes: string[] = [];
    if (draft.role !== account.role) changes.push(`change role from ${account.role} to ${draft.role}`);
    if (draft.active !== account.active) changes.push(draft.active ? 'reactivate the account' : 'deactivate the account');
    if (!changes.length) return;
    if (!window.confirm(`Confirm ${changes.join(' and ')} for ${account.email}?`)) return;
    setMessage('');
    mutation.mutate({ id: account.id, draft });
  };

  if (!users.length) return <EmptyState title="No accounts found"/>;

  return <div className="admin-list" aria-label="User accounts">
    {message && <p className="booking-notice" role="status">{message}</p>}
    {mutation.isError && <p className="booking-alert" role="alert">{getApiError(mutation.error)}</p>}
    {users.map((account) => {
      const draft = drafts[account.id] ?? { role: account.role, active: account.active };
      const isSelf = account.id === currentUserId;
      const roleLocked = isSelf || account.role === 'ADMIN';
      return <article className="admin-row" key={account.id}>
        <div><b>{account.name}</b><small>{account.email} · {account.role} · {account.active ? 'Active' : 'Inactive'}</small></div>
        <div className="admin-row-actions">
          <label className="sr-only" htmlFor={`role-${account.id}`}>Role for {account.email}</label>
          <select id={`role-${account.id}`} aria-label={`Role for ${account.email}`} value={draft.role} disabled={roleLocked || mutation.isPending}
            onChange={(event) => setDrafts({ ...drafts, [account.id]: { ...draft, role: event.target.value as Role } })}>
            {account.role === 'ADMIN' ? <option value="ADMIN">ADMIN</option> : <>
              <option value="CUSTOMER">CUSTOMER</option>
              <option value="ORGANIZER">ORGANIZER</option>
            </>}
          </select>
          <label className="active-toggle"><input type="checkbox" aria-label={`Active for ${account.email}`} checked={draft.active} disabled={isSelf || mutation.isPending}
            onChange={(event) => setDrafts({ ...drafts, [account.id]: { ...draft, active: event.target.checked } })}/> Active</label>
          <button className="button button-secondary" disabled={mutation.isPending || (draft.role === account.role && draft.active === account.active)}
            aria-label={`Save changes for ${account.email}`} onClick={() => save(account, draft)}>{mutation.isPending ? 'Saving…' : 'Save'}</button>
        </div>
      </article>;
    })}
  </div>;
}
