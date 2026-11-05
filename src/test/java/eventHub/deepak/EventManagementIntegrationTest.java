package eventHub.deepak;

import eventHub.deepak.dto.request.EventRequest;
import eventHub.deepak.enums.*;
import eventHub.deepak.entity.*;
import eventHub.deepak.repository.*;
import eventHub.deepak.service.interfaces.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest @Transactional
class EventManagementIntegrationTest {
    @Autowired EventService events; @Autowired VenueRepository venues; @Autowired ScreenRepository screens;
    @Autowired SeatCategoryRepository categories; @Autowired SeatRepository seats; @Autowired UserRepository users;

    @Test void createsOwnsAndFiltersEventsByVenueDateAndCategoryPrice() {
        User owner=user("organizer@eventhub.test",Role.ORGANIZER); User other=user("other@eventhub.test",Role.ORGANIZER);
        Venue venue=venues.save(Venue.builder().name("Central Hall").city("Delhi").build());
        Screen screen=screens.save(Screen.builder().name("Main").venue(venue).build());
        SeatCategory category=categories.save(SeatCategory.builder().name("Premium").price(new BigDecimal("500.00")).build());
        seats.save(Seat.builder().identifier("A1").screen(screen).category(category).build());
        LocalDateTime start=LocalDateTime.of(2027,2,10,19,30);
        EventRequest request=new EventRequest(); request.setName("Live show");request.setEventType(EventType.CONCERT);request.setStartTime(start);request.setVenueId(venue.getId());request.setScreenId(screen.getId());
        var created=events.createEvent(request,owner.getEmail());
        assertEquals(owner.getId(),created.getOrganizerId());
        assertEquals(1,events.getMyEvents(org.springframework.data.domain.PageRequest.of(0,10),owner.getEmail()).getTotalElements());
        assertTrue(events.getMyEvents(org.springframework.data.domain.PageRequest.of(0,10),other.getEmail()).isEmpty());
        User customer=user("customer@eventhub.test",Role.CUSTOMER);
        assertThrows(AccessDeniedException.class,()->events.getMyEvents(org.springframework.data.domain.PageRequest.of(0,10),customer.getEmail()));
        var matches=events.getAllEvents(org.springframework.data.domain.PageRequest.of(0,10),EventType.CONCERT,"delhi",start.minusDays(1),start.plusDays(1),EventStatus.BOOKING_OPEN,venue.getId(),new BigDecimal("400"),new BigDecimal("600"));
        assertEquals(1,matches.getTotalElements());
        assertThrows(AccessDeniedException.class,()->events.changeStatus(created.getId(),EventStatus.CANCELLED,other.getEmail()));
        assertTrue(events.getAllEvents(org.springframework.data.domain.PageRequest.of(0,10),null,null,null,null,null,null,null,new BigDecimal("100")).isEmpty());
    }

    private User user(String email,Role role){return users.save(User.builder().name("Test user").email(email).password("encoded-test-password").role(role).isActive(true).build());}
}
