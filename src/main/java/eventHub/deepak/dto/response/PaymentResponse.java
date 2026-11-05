package eventHub.deepak.dto.response;

import eventHub.deepak.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record PaymentResponse(Long id, Long bookingId, String provider, PaymentStatus status, BigDecimal amount,
                              String providerReference, String failureCode, LocalDateTime createdAt, LocalDateTime updatedAt) { }
