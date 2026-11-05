CREATE TABLE IF NOT EXISTS screens (
    id BIGSERIAL PRIMARY KEY, name VARCHAR(120) NOT NULL, venue_id BIGINT NOT NULL REFERENCES venue(id),
    CONSTRAINT uk_screen_venue_name UNIQUE (venue_id, name),
    CONSTRAINT uk_screen_id_venue UNIQUE (id, venue_id)
);
CREATE TABLE IF NOT EXISTS seat_categories (
    id BIGSERIAL PRIMARY KEY, name VARCHAR(80) NOT NULL UNIQUE,
    price NUMERIC(12,2) NOT NULL CHECK (price >= 0)
);
CREATE TABLE IF NOT EXISTS seats (
    id BIGSERIAL PRIMARY KEY, identifier VARCHAR(30) NOT NULL,
    screen_id BIGINT NOT NULL REFERENCES screens(id), category_id BIGINT NOT NULL REFERENCES seat_categories(id),
    CONSTRAINT uk_seat_screen_identifier UNIQUE (screen_id, identifier)
);
ALTER TABLE event ADD COLUMN IF NOT EXISTS screen_id BIGINT REFERENCES screens(id);
ALTER TABLE event ADD COLUMN IF NOT EXISTS organizer_id BIGINT REFERENCES users(id);
ALTER TABLE users ALTER COLUMN role TYPE VARCHAR(20);
UPDATE users SET role = 'CUSTOMER' WHERE role = 'USER';
UPDATE users SET role = 'CUSTOMER' WHERE role IS NULL OR role NOT IN ('CUSTOMER', 'ORGANIZER', 'ADMIN');
ALTER TABLE users ALTER COLUMN role SET NOT NULL;
ALTER TABLE users ALTER COLUMN is_active SET DEFAULT TRUE;
UPDATE users SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE users ALTER COLUMN is_active SET NOT NULL;
