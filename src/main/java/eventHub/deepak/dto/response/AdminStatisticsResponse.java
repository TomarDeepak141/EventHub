package eventHub.deepak.dto.response;

import java.math.BigDecimal;

public record AdminStatisticsResponse(long activeUsers, long customers, long organizers, long administrators,
                                      long events, long confirmedBookings, BigDecimal confirmedBookingValue,
                                      long activeTickets, long usedTickets, long cancelledTickets, long expiredTickets,
                                      long reviews, double averageRating, long favourites) { }
