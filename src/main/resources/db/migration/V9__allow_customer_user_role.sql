-- Legacy deployments may retain the old enum check that rejects CUSTOMER,
-- even though the application assigns CUSTOMER to every public registration.
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users
    ADD CONSTRAINT users_role_check CHECK (role IN ('CUSTOMER', 'ORGANIZER', 'ADMIN'));
