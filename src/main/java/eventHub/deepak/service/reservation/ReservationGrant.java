package eventHub.deepak.service.reservation;

import java.time.Instant;
import java.util.List;

public record ReservationGrant(String token, List<Long> seatIds, Instant expiresAt) {
}
