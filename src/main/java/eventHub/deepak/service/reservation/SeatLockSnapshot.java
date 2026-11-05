package eventHub.deepak.service.reservation;

import java.time.Instant;

public record SeatLockSnapshot(Long ownerId, String token, Instant expiresAt) {
}
