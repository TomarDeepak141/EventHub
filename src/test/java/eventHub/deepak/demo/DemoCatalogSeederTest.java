package eventHub.deepak.demo;

import eventHub.deepak.entity.Event;
import eventHub.deepak.enums.EventStatus;
import eventHub.deepak.enums.EventType;
import eventHub.deepak.repository.EventRepository;
import eventHub.deepak.repository.ScreenRepository;
import eventHub.deepak.repository.SeatCategoryRepository;
import eventHub.deepak.repository.SeatRepository;
import eventHub.deepak.repository.VenueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DemoCatalogSeederTest {
    @Autowired DemoCatalogSeeder seeder;
    @Autowired EventRepository events;
    @Autowired VenueRepository venues;
    @Autowired ScreenRepository screens;
    @Autowired SeatRepository seats;
    @Autowired SeatCategoryRepository categories;

    @Test
    void provisionsBrowseableCatalogAndRepeatedRunDoesNotDuplicate() {
        seeder.seed();
        seeder.seed();

        List<Event> seededEvents = events.findAll().stream()
                .filter(event -> event.getVenue().getName().startsWith("EventHub Demo · "))
                .toList();
        assertEquals(24, seededEvents.size());
        for (EventType type : List.of(EventType.CONCERT, EventType.THEATRE, EventType.MOVIE,
                EventType.SPORTS, EventType.STANDUP, EventType.WORKSHOP)) {
            List<Event> byType = seededEvents.stream().filter(event -> event.getEventType() == type).toList();
            assertEquals(4, byType.size(), type.name());
            assertTrue(byType.stream().allMatch(event -> event.getEventStatus() == EventStatus.BOOKING_OPEN));
            assertTrue(byType.stream().allMatch(event -> event.getImageUrl().startsWith("https://images.unsplash.com/")));
            assertTrue(byType.stream().allMatch(event -> event.getDescription().length() >= 80));
            assertTrue(byType.stream().allMatch(event -> !seats.findAllByScreenId(event.getScreen().getId()).isEmpty()));
        }

        assertEquals(6, venues.findAll().stream().filter(venue -> venue.getName().startsWith("EventHub Demo · ")).count());
        assertEquals(6, screens.findAll().stream().filter(screen -> screen.getName().equals("EventHub Demo Main Hall")).count());
        assertEquals(4, categories.findAll().stream().filter(category -> category.getName().startsWith("EventHub Demo ")).count());
        assertEquals(384, seats.findAll().stream()
                .filter(seat -> seat.getScreen().getName().equals("EventHub Demo Main Hall"))
                .filter(seat -> seat.getScreen().getVenue().getName().startsWith("EventHub Demo · ")).count());
    }
}
