package eventHub.deepak.service.interfaces;

import eventHub.deepak.dto.request.EventRequest;
import eventHub.deepak.dto.response.EventResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import eventHub.deepak.enums.EventType;
import eventHub.deepak.enums.EventStatus;

import java.time.LocalDateTime;
import java.math.BigDecimal;


public interface EventService {
    EventResponse createEvent(EventRequest request, String actorEmail);

    Page<EventResponse> getAllEvents(Pageable pageable, EventType type, String city, LocalDateTime from, LocalDateTime to, EventStatus status, Long venueId, BigDecimal minPrice, BigDecimal maxPrice);

    Page<EventResponse> getMyEvents(Pageable pageable, String actorEmail);

    EventResponse getEventById(Long id);

    EventResponse updateEvent(Long id, EventRequest request, String actorEmail);

    EventResponse changeStatus(Long id, EventStatus status, String actorEmail);

    void deleteEvent(Long id, String actorEmail);
}
