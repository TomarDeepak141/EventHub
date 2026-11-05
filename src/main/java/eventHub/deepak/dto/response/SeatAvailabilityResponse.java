package eventHub.deepak.dto.response;

import eventHub.deepak.enums.SeatAvailabilityStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record SeatAvailabilityResponse(Long seatId, String identifier, Long categoryId, String categoryName,
                                       BigDecimal price, SeatAvailabilityStatus status, Instant expiresAt) { }
