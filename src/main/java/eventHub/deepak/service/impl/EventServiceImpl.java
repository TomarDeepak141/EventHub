package eventHub.deepak.service.impl;

import eventHub.deepak.Repository.EventRepository;
import eventHub.deepak.Repository.VenueRepository;
import eventHub.deepak.dto.request.EventRequest;
import eventHub.deepak.dto.response.EventResponse;
import eventHub.deepak.entity.Event;
import eventHub.deepak.entity.Venue;
import eventHub.deepak.enums.EventStatus;
import eventHub.deepak.globalExceptionHandler.EventNotFoundException;
import eventHub.deepak.globalExceptionHandler.VenueNotFoundException;
import eventHub.deepak.mapper.EventMapper;
import eventHub.deepak.service.interfaces.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final EventMapper eventMapper;

    public EventServiceImpl(EventRepository eventRepository, VenueRepository venueRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.eventMapper = eventMapper;
    }
    @Override
    public EventResponse createEvent(EventRequest request){
        Venue venue =
                venueRepository.findById(request.getVenueId())
                        .orElseThrow(()->
                                new VenueNotFoundException(request.getVenueId())
                        );
        Event event= eventMapper.toEntity(request,venue);
        event.setEventStatus(EventStatus.BOOKING_OPEN);
        Event savedEvent = eventRepository.save(event);

        return eventMapper.toResponse(savedEvent);
    }

    @Override
    public Page<EventResponse> getAllEvents(Pageable pageable) {
        Page<Event> page =
                eventRepository.findAll(pageable);

        return page.map(eventMapper::toResponse);
    }

    @Override
    public EventResponse getEventById(Long id){
        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new EventNotFoundException(id)
                );

        return eventMapper.toResponse(event);
    }

    @Override
    public EventResponse updateEvent(Long id,EventRequest request){
        Event event=eventRepository
                .findById(id)
                .orElseThrow(()->
                        new EventNotFoundException(id
                        ));
        Venue venue=venueRepository
                .findById(request.getVenueId())
                .orElseThrow(()->
                        new VenueNotFoundException(request.getVenueId())
                        );
        eventMapper.updateEntity(event, request, venue);

        Event savedEvent = eventRepository.save(event);

        return eventMapper.toResponse(savedEvent);
    }

    @Override
    public void deleteEvent(Long id){
        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new EventNotFoundException(id));

        eventRepository.delete(event);
    }
}
