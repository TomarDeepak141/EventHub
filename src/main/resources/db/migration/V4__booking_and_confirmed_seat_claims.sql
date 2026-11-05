CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES users(id),
    event_id BIGINT NOT NULL REFERENCES event(id),
    status VARCHAR(24) NOT NULL CHECK (status IN ('PENDING','PAYMENT_PENDING','CONFIRMED','FAILED','CANCELLED','EXPIRED')),
    reservation_token VARCHAR(36) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    total_amount NUMERIC(12,2) NOT NULL CHECK (total_amount >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_booking_customer_event_token UNIQUE (customer_id,event_id,reservation_token)
);
CREATE INDEX ix_booking_customer_created ON bookings(customer_id,created_at);
CREATE INDEX ix_booking_event_state_expiry ON bookings(event_id,status,expires_at);

CREATE TABLE booking_items (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    event_id BIGINT NOT NULL REFERENCES event(id),
    seat_id BIGINT NOT NULL REFERENCES seats(id),
    seat_identifier_snapshot VARCHAR(30) NOT NULL,
    price_at_booking NUMERIC(12,2) NOT NULL CHECK (price_at_booking >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_booking_item_seat UNIQUE(booking_id,seat_id)
);
CREATE INDEX ix_booking_item_event_seat ON booking_items(event_id,seat_id);

CREATE TABLE confirmed_seat_claims (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES event(id),
    seat_id BIGINT NOT NULL REFERENCES seats(id),
    booking_item_id BIGINT NOT NULL REFERENCES booking_items(id),
    CONSTRAINT uk_confirmed_event_seat UNIQUE(event_id,seat_id),
    CONSTRAINT uk_confirmed_booking_item UNIQUE(booking_item_id)
);
