package eventHub.deepak.demo;

import eventHub.deepak.entity.Event;
import eventHub.deepak.entity.Screen;
import eventHub.deepak.entity.Seat;
import eventHub.deepak.entity.SeatCategory;
import eventHub.deepak.entity.Venue;
import eventHub.deepak.enums.EventStatus;
import eventHub.deepak.enums.EventType;
import eventHub.deepak.repository.EventRepository;
import eventHub.deepak.repository.ScreenRepository;
import eventHub.deepak.repository.SeatCategoryRepository;
import eventHub.deepak.repository.SeatRepository;
import eventHub.deepak.repository.VenueRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Creates the opt-in, fictional catalog without overwriting user-managed data. */
@Service
public class DemoCatalogSeeder {
    private static final String DEMO_PREFIX = "EventHub Demo · ";
    private static final List<CategorySeed> CATEGORY_SEEDS = List.of(
            new CategorySeed("EventHub Demo Economy", "35.00"),
            new CategorySeed("EventHub Demo Standard", "60.00"),
            new CategorySeed("EventHub Demo Premium", "95.00"),
            new CategorySeed("EventHub Demo VIP", "140.00")
    );
    private static final List<CatalogEvent> CATALOG = buildCatalog();

    private final VenueRepository venues;
    private final ScreenRepository screens;
    private final SeatRepository seats;
    private final SeatCategoryRepository categories;
    private final EventRepository events;

    public DemoCatalogSeeder(VenueRepository venues, ScreenRepository screens, SeatRepository seats,
                             SeatCategoryRepository categories, EventRepository events) {
        this.venues = venues;
        this.screens = screens;
        this.seats = seats;
        this.categories = categories;
        this.events = events;
    }

    @Transactional
    public void seed() {
        List<SeatCategory> seatCategories = CATEGORY_SEEDS.stream()
                .map(this::findOrCreateCategory)
                .toList();

        for (CatalogEvent catalogEvent : CATALOG) {
            Venue venue = findOrCreateVenue(catalogEvent);
            Screen screen = findOrCreateScreen(venue);
            ensureSeats(screen, seatCategories);
            if (!events.existsByNameIgnoreCaseAndEventType(catalogEvent.title(), catalogEvent.type())) {
                events.save(Event.builder()
                        .name(catalogEvent.title())
                        .description(catalogEvent.description())
                        .eventType(catalogEvent.type())
                        .eventStatus(EventStatus.BOOKING_OPEN)
                        .language(catalogEvent.language())
                        .organizer(catalogEvent.presenter())
                        .imageUrl(catalogEvent.imageUrl())
                        .startTime(LocalDateTime.now().plusDays(catalogEvent.daysFromSeed()))
                        .duration(Duration.ofMinutes(catalogEvent.durationMinutes()))
                        .venue(venue)
                        .screen(screen)
                        .build());
            }
        }
    }

    private SeatCategory findOrCreateCategory(CategorySeed seed) {
        return categories.findAll().stream()
                .filter(category -> category.getName().equalsIgnoreCase(seed.name()))
                .findFirst()
                .orElseGet(() -> categories.save(SeatCategory.builder()
                        .name(seed.name())
                        .price(new BigDecimal(seed.price()))
                        .build()));
    }

    private Venue findOrCreateVenue(CatalogEvent seed) {
        String name = DEMO_PREFIX + seed.venueName();
        return venues.findAll().stream()
                .filter(venue -> venue.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> venues.save(Venue.builder().name(name).city(seed.city()).build()));
    }

    private Screen findOrCreateScreen(Venue venue) {
        String name = "EventHub Demo Main Hall";
        return screens.findAll().stream()
                .filter(screen -> screen.getVenue().getId().equals(venue.getId()))
                .filter(screen -> screen.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> screens.save(Screen.builder().name(name).venue(venue).build()));
    }

    private void ensureSeats(Screen screen, List<SeatCategory> categories) {
        List<Seat> existing = seats.findAllByScreenId(screen.getId());
        List<Seat> missing = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int number = 1; number <= 8; number++) {
                String identifier = String.valueOf((char) ('A' + row)) + number;
                boolean present = existing.stream().anyMatch(seat -> seat.getIdentifier().equalsIgnoreCase(identifier));
                if (!present) {
                    int categoryIndex = row < 3 ? 0 : row < 5 ? 1 : row < 7 ? 2 : 3;
                    missing.add(Seat.builder().identifier(identifier).screen(screen)
                            .category(categories.get(categoryIndex)).build());
                }
            }
        }
        if (!missing.isEmpty()) {
            seats.saveAll(missing);
        }
    }

    private static List<CatalogEvent> buildCatalog() {
        List<CatalogEvent> events = new ArrayList<>();
        add(events, EventType.CONCERT, "Mumbai", "Aurora Arena", "Lumen Live",
                "photo-1501386761578-eac5c94b800a", 155, List.of(
                        event("Neon Skyline: Live in Mumbai", "A night of luminous synths, live drums, and city-sized hooks. An acclaimed six-piece closes the show with a full-band encore and immersive light design.", 18),
                        event("Monsoon Sessions: The Riverside Set", "Intimate acoustic arrangements meet a warm open-air production. Doors open early for local singer-songwriters before the headline trio takes the stage.", 34),
                        event("Roots & Rhythm Festival", "A celebration of contemporary Indian folk, percussion, and global roots music. Three distinct sets share one stage, with comfortable seating and clear sightlines throughout.", 52),
                        event("The Midnight Orchestra", "A cinematic live performance blending a chamber orchestra with modern electronic production. Hear familiar film and game themes reimagined for a sweeping concert hall.", 76)));
        add(events, EventType.THEATRE, "Delhi", "Grand Stage Theatre", "StageCraft Company",
                "photo-1503095396549-807759245b35", 145, List.of(
                        event("The Last Train to Connaught Place", "A new ensemble drama follows six strangers whose late-night journey becomes a funny, moving portrait of a changing city.", 20),
                        event("A Midsummer Night's Dream", "Shakespeare's playful forest comedy returns in a vivid, family-friendly production with live music, bold costumes, and a talented young cast.", 38),
                        event("Monologues from Home", "Four performers share sharply observed stories about family, ambition, and belonging. Each story stands alone before the voices meet in a memorable final scene.", 58),
                        event("The Great Indian Heist", "A fast-paced caper built around mistaken identities, clever stagecraft, and a plan that keeps changing. Expect a light-hearted evening of twists and ensemble comedy.", 82)));
        add(events, EventType.MOVIE, "Bengaluru", "Starlight Cinema House", "Starlight Repertory",
                "photo-1489599849927-2ee91cede3ba", 135, List.of(
                        event("Orbiters: A Deep Space Story", "A beautifully photographed science-fiction adventure about a repair crew racing to bring a stranded research station home. Presented in a premium large-format screening.", 16),
                        event("The Paper Garden", "An award-winning independent drama about an illustrator rebuilding her family's old bookshop. The screening includes a short introduction from the local film society.", 32),
                        event("Monsoon Express", "A charming road comedy follows two cousins, one borrowed car, and a wedding deadline across the Western Ghats. A bright crowd-pleaser for a relaxed cinema night.", 49),
                        event("The Silent Frequency", "A suspenseful mystery set inside a community radio station during a city-wide blackout. Enjoy crisp surround sound, a brief intermission, and a spoiler-free post-film discussion.", 68)));
        add(events, EventType.SPORTS, "Pune", "Riverside Sports Bowl", "Cityside Athletics",
                "photo-1461896836934-ffe607ba8211", 180, List.of(
                        event("Cityside United: Season Opener", "Catch the local football club at home for the first match of the season. General seating, family sections, and accessible entry are available around the ground.", 14),
                        event("Champions Court: Pro Basketball Night", "A high-energy professional basketball double-header featuring fast breaks, skill contests, and a short half-time show. Arrive early for warm-ups and player introductions.", 29),
                        event("Pune Track & Field Grand Prix", "Regional athletes compete across sprints, jumps, and middle-distance races in an all-afternoon programme. The schedule includes a junior relay showcase.", 47),
                        event("Riverside Rugby Sevens", "A lively one-day sevens tournament with quick matches, local food stalls, and shaded family seating. The final is scheduled under the stadium lights.", 63)));
        add(events, EventType.STANDUP, "Hyderabad", "The Laughing Room", "Good Evening Comedy",
                "photo-1585699324551-f6c309eedeca", 110, List.of(
                        event("Asha Rao: Room for One More", "Asha's new hour turns everyday errands, family group chats, and big life plans into warm, sharply timed observations. Recommended for adult audiences.", 22),
                        event("The Hyderabad Comedy Mix", "Four rising comics take turns with fresh material in a relaxed club setting. The lineup is announced at the door, and every performer brings a distinctly different point of view.", 40),
                        event("Punchlines & Chai", "An early-evening showcase pairing short stand-up sets with a cosy café atmosphere. Come for an easy midweek laugh and stay for the friendly post-show meet-up.", 60),
                        event("Late Night: No Notes", "A loose, energetic late show built around quick stories, playful crowd work, and spontaneous riffs. Seating is limited for a closer view of the stage.", 78)));
        add(events, EventType.WORKSHOP, "Chennai", "Makers Guild Studio", "Makers Guild",
                "photo-1521737711867-e3b97375f902", 120, List.of(
                        event("Build a Better Story: Writing Lab", "A practical guided session on turning a rough idea into a compelling short story. Bring a notebook; all exercises, examples, and peer feedback are included.", 19),
                        event("Street Photography Walkshop", "Learn to read light, frame candid scenes, and build a respectful street photography practice. Bring a charged camera or phone; the route is accessible and beginner-friendly.", 36),
                        event("Make Your First Ceramic Mug", "Shape and decorate a one-of-a-kind mug with a local ceramic artist. Clay, tools, firing, and an apron are included; finished pieces can be collected after firing.", 54),
                        event("Creative Coding with p5.js", "A hands-on introduction to making interactive visuals with a few approachable lines of code. No programming background is needed; laptops and guided starter files are provided.", 73)));
        return List.copyOf(events);
    }

    private static void add(List<CatalogEvent> target, EventType type, String city, String venue,
                            String presenter, String photoId, int duration, List<EventSeed> seeds) {
        for (int i = 0; i < seeds.size(); i++) {
            EventSeed seed = seeds.get(i);
            target.add(new CatalogEvent(type, seed.title(), seed.description(), city, venue, presenter,
                    "https://images.unsplash.com/" + photoId + "?auto=format&fit=crop&w=1200&q=82",
                    seed.daysFromSeed(), duration, i % 2 == 0 ? "English" : "English, Hindi"));
        }
    }

    private static EventSeed event(String title, String description, int daysFromSeed) {
        return new EventSeed(title, description, daysFromSeed);
    }

    private record CategorySeed(String name, String price) { }
    private record EventSeed(String title, String description, int daysFromSeed) { }
    private record CatalogEvent(EventType type, String title, String description, String city,
                                String venueName, String presenter, String imageUrl, int daysFromSeed,
                                int durationMinutes, String language) { }
}
