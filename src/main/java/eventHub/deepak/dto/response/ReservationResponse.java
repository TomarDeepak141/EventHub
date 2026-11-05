package eventHub.deepak.dto.response;

import java.time.Instant;
import java.util.List;

public record ReservationResponse(String reservationToken, Long eventId, List<Long> seatIds, Instant expiresAt) { }
