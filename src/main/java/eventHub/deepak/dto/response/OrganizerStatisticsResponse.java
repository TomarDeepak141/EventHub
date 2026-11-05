package eventHub.deepak.dto.response;

import org.springframework.data.domain.Page;
import java.math.BigDecimal;

public record OrganizerStatisticsResponse(long ownedEvents, long confirmedBookings, BigDecimal confirmedBookingValue,
                                          long ticketsIssued, long reviews, double averageRating,
                                          Page<EventStatisticsResponse> events) { }
