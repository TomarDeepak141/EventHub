package eventHub.deepak.service.impl;

import eventHub.deepak.repository.*;
import eventHub.deepak.dto.request.EventRequest;
import eventHub.deepak.dto.response.EventResponse;
import eventHub.deepak.entity.*;
import eventHub.deepak.enums.*;
import eventHub.deepak.globalExceptionHandler.EventNotFoundException;
import eventHub.deepak.globalExceptionHandler.VenueNotFoundException;
import eventHub.deepak.mapper.EventMapper;
import eventHub.deepak.service.interfaces.EventService;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Set;

import org.springframework.data.domain.Sort;
import eventHub.deepak.util.PageableSupport;

@Service
@Transactional
public class EventServiceImpl implements EventService {
    private final EventRepository events;
    private final VenueRepository venues;
    private final ScreenRepository screens;
    private final UserRepository users;
    private final EventMapper mapper;

    public EventServiceImpl(EventRepository e, VenueRepository v, ScreenRepository s, UserRepository u, EventMapper m) {
        events = e;
        venues = v;
        screens = s;
        users = u;
        mapper = m;
    }

    @Override
    public EventResponse createEvent(EventRequest request, String actorEmail) {
        User actor = findUser(actorEmail);
        if (actor.getRole() == Role.CUSTOMER)
            throw new AccessDeniedException("Only organizers and admins can create events");
        Venue venue = venues.findById(request.getVenueId()).orElseThrow(() -> new VenueNotFoundException(request.getVenueId()));
        Screen screen = screens.findById(request.getScreenId()).orElseThrow(() -> new IllegalArgumentException("Screen not found: " + request.getScreenId()));
        if (!screen.getVenue().getId().equals(venue.getId()))
            throw new IllegalArgumentException("Screen must belong to the selected venue");
        User owner = actor.getRole() == Role.ADMIN && request.getOrganizerId() != null ? users.findById(request.getOrganizerId()).orElseThrow(() -> new IllegalArgumentException("Organizer not found: " + request.getOrganizerId())) : actor;
        if (owner.getRole() != Role.ORGANIZER && owner.getRole() != Role.ADMIN)
            throw new IllegalArgumentException("Event owner must be an organizer or admin");
        Event event = mapper.toEntity(request, venue);
        event.setScreen(screen);
        event.setOrganizerUser(owner);
        event.setEventStatus(EventStatus.BOOKING_OPEN);
        return mapper.toResponse(events.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponse> getAllEvents(Pageable p, EventType type, String city, LocalDateTime from, LocalDateTime to, EventStatus status, Long venueId, BigDecimal minPrice, BigDecimal maxPrice) {
        Specification<Event> spec = (root, q, cb) -> cb.conjunction();
        if (type != null) spec = spec.and((r, q, c) -> c.equal(r.get("eventType"), type));
        if (status != null) spec = spec.and((r, q, c) -> c.equal(r.get("eventStatus"), status));
        if (venueId != null) spec = spec.and((r, q, c) -> c.equal(r.get("venue").get("id"), venueId));
        if (city != null && !city.isBlank())
            spec = spec.and((r, q, c) -> c.like(c.lower(r.get("venue").get("city")), "%" + city.trim().toLowerCase() + "%"));
        if (from != null) spec = spec.and((r, q, c) -> c.greaterThanOrEqualTo(r.get("startTime"), from));
        if (to != null) spec = spec.and((r, q, c) -> c.lessThanOrEqualTo(r.get("startTime"), to));
        if (minPrice != null || maxPrice != null) spec = spec.and((r, q, c) -> {
            q.distinct(true);
            var s = r.join("screen");
            var seat = s.join("seats");
            var category = seat.join("category");
            if (minPrice != null && maxPrice != null) return c.between(category.get("price"), minPrice, maxPrice);
            if (minPrice != null) return c.greaterThanOrEqualTo(category.get("price"), minPrice);
            return c.lessThanOrEqualTo(category.get("price"), maxPrice);
        });
        if (from != null && to != null && from.isAfter(to))
            throw new IllegalArgumentException("from must not be after to");
        if ((minPrice != null && minPrice.signum() < 0) || (maxPrice != null && maxPrice.signum() < 0))
            throw new IllegalArgumentException("Prices cannot be negative");
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0)
            throw new IllegalArgumentException("minPrice must not exceed maxPrice");
        Pageable safe = PageableSupport.validate(p, Set.of("id", "name", "startTime", "eventType", "eventStatus", "createdAt", "updatedAt"),
                Sort.by(Sort.Order.asc("startTime"), Sort.Order.asc("id")));
        return events.findAll(spec, safe).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponse> getMyEvents(Pageable pageable, String actorEmail) {
        User actor = findUser(actorEmail);
        if (actor.getRole() != Role.ORGANIZER || !Boolean.TRUE.equals(actor.getIsActive()))
            throw new AccessDeniedException("An active organizer account is required");
        Pageable safe = PageableSupport.validate(pageable, Set.of("id", "name", "startTime", "eventType", "eventStatus", "createdAt", "updatedAt"),
                Sort.by(Sort.Order.asc("startTime"), Sort.Order.asc("id")));
        return events.findByOrganizerUserId(actor.getId(), safe).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        return mapper.toResponse(find(id));
    }

    @Override
    public EventResponse updateEvent(Long id, EventRequest request, String actorEmail) {
        Event e = find(id);
        authorize(e, actorEmail);
        Venue venue = venues.findById(request.getVenueId()).orElseThrow(() -> new VenueNotFoundException(request.getVenueId()));
        Screen screen = screens.findById(request.getScreenId()).orElseThrow(() -> new IllegalArgumentException("Screen not found: " + request.getScreenId()));
        if (!screen.getVenue().getId().equals(venue.getId()))
            throw new IllegalArgumentException("Screen must belong to the selected venue");
        mapper.updateEntity(e, request, venue);
        e.setScreen(screen);
        return mapper.toResponse(e);
    }

    @Override
    public EventResponse changeStatus(Long id, EventStatus status, String actorEmail) {
        Event e = find(id);
        authorize(e, actorEmail);
        e.setEventStatus(status);
        return mapper.toResponse(e);
    }

    @Override
    public void deleteEvent(Long id, String actorEmail) {
        Event e = find(id);
        authorize(e, actorEmail);
        events.delete(e);
    }

    private Event find(Long id) {
        return events.findById(id).orElseThrow(() -> new EventNotFoundException(id));
    }

    private User findUser(String email) {
        return users.findByEmail(email).orElseThrow(() -> new AccessDeniedException("Account is unavailable"));
    }

    private void authorize(Event e, String email) {
        User actor = findUser(email);
        if (actor.getRole() != Role.ADMIN && (e.getOrganizerUser() == null || !e.getOrganizerUser().getId().equals(actor.getId())))
            throw new AccessDeniedException("You may manage only your own events");
    }
}
