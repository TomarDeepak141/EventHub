package eventHub.deepak.dto.response;

import eventHub.deepak.enums.EventStatus;
import java.math.BigDecimal;

public record EventStatisticsResponse(Long eventId, String eventName, EventStatus status, long confirmedBookings,
                                      BigDecimal confirmedBookingValue, long issuedTickets, long reviewCount,
                                      double averageRating) { }
