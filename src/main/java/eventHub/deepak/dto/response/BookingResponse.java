package eventHub.deepak.dto.response;

import eventHub.deepak.enums.BookingStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(Long id, Long eventId, BookingStatus status, BigDecimal totalAmount,
                              Instant expiresAt, LocalDateTime createdAt, List<BookingItemResponse> items) { }
