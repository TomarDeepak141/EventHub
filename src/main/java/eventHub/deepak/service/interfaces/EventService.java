package eventHub.deepak.service.interfaces;

import eventHub.deepak.dto.request.EventRequest;
import eventHub.deepak.dto.response.EventResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface EventService {
    EventResponse createEvent(EventRequest request);
    Page<EventResponse> getAllEvents(Pageable pageable);
    EventResponse getEventById(Long id);
    EventResponse updateEvent(Long id,EventRequest request);
    void deleteEvent(Long id);
}
