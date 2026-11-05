-- Optional local demo catalog only. This script creates no users, passwords,
-- admin accounts, payments, or tickets and is never run automatically.
-- Execute manually against a disposable local database after Flyway succeeds.

INSERT INTO venue (name, city)
SELECT 'EventHub Demo Hall', 'Northstar City'
WHERE NOT EXISTS (SELECT 1 FROM venue WHERE name = 'EventHub Demo Hall' AND city = 'Northstar City');

INSERT INTO venue (name, city)
SELECT 'Harborlight Grounds', 'Northstar City'
WHERE NOT EXISTS (SELECT 1 FROM venue WHERE name = 'Harborlight Grounds' AND city = 'Northstar City');

INSERT INTO seat_categories (name, price) VALUES
    ('Demo Standard', 35.00),
    ('Demo Premium', 65.00)
ON CONFLICT DO NOTHING;

INSERT INTO screens (name, venue_id)
SELECT screen.name, venue.id
FROM (VALUES ('Main Stage', 'EventHub Demo Hall'), ('Studio Room', 'EventHub Demo Hall'), ('Open Air Stage', 'Harborlight Grounds')) AS screen(name, venue_name)
JOIN venue ON venue.name = screen.venue_name AND venue.city = 'Northstar City'
WHERE NOT EXISTS (
    SELECT 1 FROM screens existing
    WHERE existing.venue_id = venue.id AND lower(existing.name) = lower(screen.name)
);

INSERT INTO seats (identifier, screen_id, category_id)
SELECT 'A' || lpad(number::text, 2, '0'), screen.id, category.id
FROM screens screen
JOIN venue ON venue.id = screen.venue_id
CROSS JOIN generate_series(1, 24) AS number
JOIN seat_categories category ON category.name = CASE WHEN number <= 12 THEN 'Demo Standard' ELSE 'Demo Premium' END
WHERE venue.city = 'Northstar City'
  AND venue.name IN ('EventHub Demo Hall', 'Harborlight Grounds')
  AND screen.name IN ('Main Stage', 'Studio Room', 'Open Air Stage')
  AND NOT EXISTS (
      SELECT 1 FROM seats existing
      WHERE existing.screen_id = screen.id AND lower(existing.identifier) = lower('A' || lpad(number::text, 2, '0'))
  );

INSERT INTO event (name, description, event_type, event_status, start_time, duration, venue_id, screen_id, organizer)
SELECT demo.name, demo.description, demo.event_type, 'BOOKING_OPEN', CURRENT_TIMESTAMP + demo.days_ahead * INTERVAL '1 day',
       demo.duration, venue.id, screen.id, 'EventHub Demo Organizer'
FROM (VALUES
    ('Demo: Aurora Sound Sessions', 'A fictional live music night for trying discovery and seat selection.', 'CONCERT', 21, INTERVAL '2 hours', 'EventHub Demo Hall', 'Main Stage'),
    ('Demo: Lantern Stories', 'A fictional theatre event for exploring EventHub.', 'THEATRE', 28, INTERVAL '2 hours', 'EventHub Demo Hall', 'Studio Room'),
    ('Demo: Skyline Cup Final', 'A fictional sports event for the seat reservation demo.', 'SPORTS', 35, INTERVAL '3 hours', 'Harborlight Grounds', 'Open Air Stage')
) AS demo(name, description, event_type, days_ahead, duration, venue_name, screen_name)
JOIN venue ON venue.name = demo.venue_name AND venue.city = 'Northstar City'
JOIN screens screen ON screen.venue_id = venue.id AND screen.name = demo.screen_name
WHERE NOT EXISTS (SELECT 1 FROM event existing WHERE existing.name = demo.name);

SELECT name, event_type, start_time, venue_id, screen_id
FROM event
WHERE name LIKE 'Demo:%'
ORDER BY start_time;
