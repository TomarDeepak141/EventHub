ALTER TABLE venue ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE venue ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE screens ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE screens ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE seats ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE seats ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE seat_categories ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE seat_categories ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
UPDATE users SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
UPDATE users SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;
UPDATE event SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
UPDATE event SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;
ALTER TABLE users ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE users ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE event ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE event ALTER COLUMN updated_at SET NOT NULL;
INSERT INTO venue(name, city) SELECT 'Legacy Venue', 'Unknown' WHERE NOT EXISTS (SELECT 1 FROM venue);
UPDATE venue SET name = 'Venue ' || id WHERE name IS NULL OR trim(name) = '';
UPDATE venue SET city = 'Unknown' WHERE city IS NULL OR trim(city) = '';
ALTER TABLE venue ALTER COLUMN name SET NOT NULL;
ALTER TABLE venue ALTER COLUMN city SET NOT NULL;
UPDATE event SET event_status = 'BOOKING_OPEN' WHERE event_status IS NULL;
UPDATE event SET name = 'Event ' || id WHERE name IS NULL OR trim(name) = '';
UPDATE event SET start_time = CURRENT_TIMESTAMP WHERE start_time IS NULL;
UPDATE event SET venue_id = (SELECT min(id) FROM venue) WHERE venue_id IS NULL;
ALTER TABLE event ALTER COLUMN name SET NOT NULL;
ALTER TABLE event ALTER COLUMN event_status SET NOT NULL;
ALTER TABLE event ALTER COLUMN start_time SET NOT NULL;
ALTER TABLE event ALTER COLUMN venue_id SET NOT NULL;
INSERT INTO screens(name, venue_id)
SELECT 'Default', v.id FROM venue v
WHERE NOT EXISTS (SELECT 1 FROM screens s WHERE s.venue_id = v.id);
UPDATE event e SET screen_id = (
    SELECT min(s.id) FROM screens s WHERE s.venue_id = e.venue_id
) WHERE e.screen_id IS NULL;
ALTER TABLE event ALTER COLUMN screen_id SET NOT NULL;
ALTER TABLE users ALTER COLUMN name TYPE VARCHAR(120);
ALTER TABLE users ALTER COLUMN email TYPE VARCHAR(254);
ALTER TABLE users ALTER COLUMN password TYPE VARCHAR(100);
ALTER TABLE venue ALTER COLUMN name TYPE VARCHAR(160);
ALTER TABLE venue ALTER COLUMN city TYPE VARCHAR(120);
ALTER TABLE event ALTER COLUMN name TYPE VARCHAR(200);
ALTER TABLE event ALTER COLUMN event_type TYPE VARCHAR(40);
ALTER TABLE event ALTER COLUMN event_status TYPE VARCHAR(40);
ALTER TABLE event ALTER COLUMN language TYPE VARCHAR(100);
ALTER TABLE event ALTER COLUMN organizer TYPE VARCHAR(200);
ALTER TABLE event ALTER COLUMN description TYPE TEXT;
ALTER TABLE event ALTER COLUMN image_url TYPE TEXT;
CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email_ci ON users (lower(email));
CREATE UNIQUE INDEX IF NOT EXISTS uk_screen_venue_name_ci ON screens (venue_id, lower(name));
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE contype = 'u'
          AND conrelid = 'screens'::regclass
          AND pg_get_constraintdef(oid) = 'UNIQUE (id, venue_id)'
    ) THEN
        ALTER TABLE screens ADD CONSTRAINT uk_screens_id_venue UNIQUE (id, venue_id);
    END IF;
END $$;
CREATE UNIQUE INDEX IF NOT EXISTS uk_seat_screen_identifier_ci ON seats (screen_id, lower(identifier));
CREATE UNIQUE INDEX IF NOT EXISTS uk_seat_category_name_ci ON seat_categories (lower(name));
CREATE INDEX IF NOT EXISTS ix_event_start_time ON event (start_time);
CREATE INDEX IF NOT EXISTS ix_event_status_type ON event (event_status, event_type);
CREATE INDEX IF NOT EXISTS ix_event_venue ON event (venue_id);
ALTER TABLE event ADD CONSTRAINT fk_event_screen_venue FOREIGN KEY (screen_id, venue_id) REFERENCES screens(id, venue_id);
