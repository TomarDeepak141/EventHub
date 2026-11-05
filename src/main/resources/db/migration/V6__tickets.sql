CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    public_reference VARCHAR(64) NOT NULL,
    booking_item_id BIGINT NOT NULL REFERENCES booking_items(id),
    event_id BIGINT NOT NULL REFERENCES event(id),
    status VARCHAR(16) NOT NULL CHECK (status IN ('ACTIVE','USED','CANCELLED','EXPIRED')),
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ticket_public_reference UNIQUE(public_reference),
    CONSTRAINT uk_ticket_booking_item UNIQUE(booking_item_id)
);
CREATE INDEX ix_ticket_event_status ON tickets(event_id,status);
CREATE INDEX ix_ticket_expiry ON tickets(status,expires_at);
