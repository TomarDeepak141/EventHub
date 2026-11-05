package eventHub.deepak.controller;

import eventHub.deepak.dto.request.*;
import eventHub.deepak.dto.response.EventResponse;
import eventHub.deepak.enums.*;
import eventHub.deepak.service.interfaces.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController @RequestMapping("/events")
public class EventController {
    private final EventService service;
    public EventController(EventService s){service=s;}
    @PostMapping @Operation(security=@SecurityRequirement(name="bearerAuth")) public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest r,Authentication a){return ResponseEntity.status(HttpStatus.CREATED).body(service.createEvent(r,a.getName()));}
    @GetMapping public Page<EventResponse> all(Pageable pageable,@RequestParam(required=false) EventType type,@RequestParam(required=false) String city,@RequestParam(required=false) LocalDateTime from,@RequestParam(required=false) LocalDateTime to,@RequestParam(required=false) EventStatus status,@RequestParam(required=false) Long venueId,@RequestParam(required=false) BigDecimal minPrice,@RequestParam(required=false) BigDecimal maxPrice){return service.getAllEvents(pageable,type,city,from,to,status,venueId,minPrice,maxPrice);}
    @GetMapping("/mine") @PreAuthorize("hasRole('ORGANIZER')") @Operation(summary="List events owned by the authenticated organizer",security=@SecurityRequirement(name="bearerAuth")) public Page<EventResponse> mine(Pageable pageable,Authentication authentication){return service.getMyEvents(pageable,authentication.getName());}
    @GetMapping("/{id}") public EventResponse get(@PathVariable Long id){return service.getEventById(id);}
    @PutMapping("/{id}") @Operation(security=@SecurityRequirement(name="bearerAuth")) public EventResponse update(@PathVariable Long id,@Valid @RequestBody EventRequest r,Authentication a){return service.updateEvent(id,r,a.getName());}
    @PatchMapping("/{id}/status") @Operation(security=@SecurityRequirement(name="bearerAuth")) public EventResponse status(@PathVariable Long id,@Valid @RequestBody EventStatusRequest r,Authentication a){return service.changeStatus(id,r.status(),a.getName());}
    @DeleteMapping("/{id}") @Operation(security=@SecurityRequirement(name="bearerAuth")) public ResponseEntity<Void> delete(@PathVariable Long id,Authentication a){service.deleteEvent(id,a.getName());return ResponseEntity.noContent().build();}
}
