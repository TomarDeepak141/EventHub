package eventHub.deepak.mapper;

import eventHub.deepak.dto.request.EventRequest;
import eventHub.deepak.dto.response.EventResponse;
import eventHub.deepak.entity.Event;
import eventHub.deepak.entity.Venue;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {
    public Event toEntity(EventRequest request, Venue venue){
        Event event = new Event();

        event.setName(request.getName());

        event.setDuration(request.getDuration());
        event.setDescription(request.getDescription());
        event.setLanguage(request.getLanguage());
        event.setOrganizer(request.getOrganizer());
        event.setImageUrl(request.getImageUrl());
        event.setStartTime(request.getStartTime());
        event.setEventType(request.getEventType());
        event.setVenue(venue);


        return event;
    }
    public EventResponse toResponse(Event event){
        EventResponse response=new EventResponse();

        response.setId(event.getId());
        response.setEventType(event.getEventType());
        response.setDuration(event.getDuration());
        response.setDescription(event.getDescription());
        response.setLanguage(event.getLanguage());
        response.setOrganizer(event.getOrganizer());
        response.setName(event.getName());
        response.setCity(event.getVenue().getCity());
        response.setImageUrl(event.getImageUrl());
        response.setEventStatus(event.getEventStatus());
        response.setStartTime(event.getStartTime());
        response.setVenueName(event.getVenue().getName());

        return response;
    }
    public void updateEntity(
            Event event,
            EventRequest request,
            Venue venue
    ){
        event.setName(request.getName());

        event.setDuration(request.getDuration());
        event.setDescription(request.getDescription());
        event.setLanguage(request.getLanguage());
        event.setOrganizer(request.getOrganizer());
        event.setImageUrl(request.getImageUrl());
        event.setStartTime(request.getStartTime());
        event.setEventType(request.getEventType());
        event.setVenue(venue);

    }
}
