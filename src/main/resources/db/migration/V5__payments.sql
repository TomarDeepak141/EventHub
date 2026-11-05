CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id),
    idempotency_key VARCHAR(80) NOT NULL,
    provider VARCHAR(32) NOT NULL,
    status VARCHAR(24) NOT NULL CHECK (status IN ('INITIATED','PROCESSING','SUCCEEDED','FAILED','CANCELLED','REFUND_PENDING','REFUNDED')),
    amount NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
    provider_reference VARCHAR(100),
    failure_code VARCHAR(80),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_payment_booking_idempotency UNIQUE(booking_id,idempotency_key)
);
CREATE INDEX ix_payment_booking_created ON payments(booking_id,created_at);
