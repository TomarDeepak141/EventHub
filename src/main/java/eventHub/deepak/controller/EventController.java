package eventHub.deepak.controller;

import eventHub.deepak.dto.request.EventRequest;
import eventHub.deepak.dto.response.EventResponse;
import eventHub.deepak.service.interfaces.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventRequest request){
        EventResponse response = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);

    }
    @GetMapping
    public ResponseEntity<Page<EventResponse>> getAllEvents(
            Pageable pageable
    ){
        return ResponseEntity.ok(
                eventService.getAllEvents(pageable)
        );
    }
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                eventService.getEventById(id)
        );
    }
    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request
    ) {
        return ResponseEntity.ok(
                eventService.updateEvent(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id
    ){
        eventService.deleteEvent(id);

        return ResponseEntity.noContent().build();
    }

}
