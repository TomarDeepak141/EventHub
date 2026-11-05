package eventHub.deepak.dto.response;

import java.math.BigDecimal;

public record BookingItemResponse(Long id, Long seatId, String seatIdentifier, BigDecimal priceAtBooking) { }
