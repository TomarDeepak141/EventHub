package eventHub.deepak.service.impl;

import eventHub.deepak.repository.*;
import eventHub.deepak.dto.request.*;
import eventHub.deepak.dto.response.*;
import eventHub.deepak.entity.*;
import eventHub.deepak.globalExceptionHandler.VenueNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

@Service
@Transactional
public class CatalogService {
    private final VenueRepository venues;
    private final ScreenRepository screens;
    private final SeatRepository seats;
    private final SeatCategoryRepository categories;

    public CatalogService(VenueRepository v, ScreenRepository s, SeatRepository st, SeatCategoryRepository c) {
        venues = v;
        screens = s;
        seats = st;
        categories = c;
    }

    public List<ScreenResponse> screens() {
        return screens.findAll().stream().map(this::screenDto).toList();
    }

    public ScreenResponse createScreen(ScreenRequest r) {
        Venue v = venues.findById(r.venueId())
                .orElseThrow(() ->
                        new VenueNotFoundException(r.venueId())
                );
        if (screens.existsByVenueIdAndNameIgnoreCase(v.getId(), r.name().trim()))
            throw new DataIntegrityViolationException("Duplicate screen name for venue");
        return screenDto(screens.save(Screen.builder().name(r.name().trim()).venue(v).build()));
    }

    public ScreenResponse updateScreen(Long id, ScreenRequest r) {
        Screen x = screens.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Screen not found: " + id)
                );
        Venue v = venues.findById(r.venueId())
                .orElseThrow(() ->
                        new VenueNotFoundException(r.venueId())
                );
        if (screens.existsByVenueIdAndNameIgnoreCaseAndIdNot(v.getId(), r.name().trim(), id))
            throw new DataIntegrityViolationException("Duplicate screen name for venue");
        x.setName(r.name().trim());
        x.setVenue(v);
        return screenDto(x);
    }

    public void deleteScreen(Long id) {
        screens.delete(screens.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Screen not found: " + id)
                ));
    }

    public List<SeatCategoryResponse> categories() {
        return categories
                .findAll()
                .stream()
                .map(this::categoryDto)
                .toList();
    }

    public SeatCategoryResponse createCategory(SeatCategoryRequest r) {
        if (categories.existsByNameIgnoreCase(r.name().trim()))
            throw
                    new DataIntegrityViolationException("Seat category already exists");

        return categoryDto(categories
                .save(SeatCategory
                        .builder()
                        .name(r.name().trim())
                        .price(r.price())
                        .build())
        );
    }

    public SeatCategoryResponse updateCategory(Long id, SeatCategoryRequest r) {
        SeatCategory x = categories
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Seat category not found: " + id)
                );
        if (categories.existsByNameIgnoreCaseAndIdNot(r.name().trim(), id))
            throw new
                    DataIntegrityViolationException("Seat category already exists");
        x.setName(r.name().trim());
        x.setPrice(r.price());
        return categoryDto(x);
    }

    public void deleteCategory(Long id) {
        categories
                .delete(categories
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Seat category not found: " + id)
                        ));
    }

    public List<SeatResponse> seats(Long screenId) {
        return seats.findAllByScreenId(screenId).stream().map(this::seatDto).toList();
    }

    public SeatResponse createSeat(SeatRequest r) {
        Screen screen = screens.findById(r.screenId()).orElseThrow(() -> new IllegalArgumentException("Screen not found: " + r.screenId()));
        SeatCategory cat = categories.findById(r.categoryId()).orElseThrow(() -> new IllegalArgumentException("Seat category not found: " + r.categoryId()));
        if (seats.existsByScreenIdAndIdentifierIgnoreCase(r.screenId(), r.identifier().trim()))
            throw new DataIntegrityViolationException("Seat identifier already exists in screen");
        return seatDto(seats.save(Seat.builder().screen(screen).category(cat).identifier(r.identifier().trim()).build()));
    }

    public SeatResponse updateSeat(Long id, SeatRequest r) {
        Seat s = seats.findById(id).orElseThrow(() -> new IllegalArgumentException("Seat not found: " + id));
        if (seats.existsByScreenIdAndIdentifierIgnoreCaseAndIdNot(r.screenId(), r.identifier().trim(), id))
            throw new DataIntegrityViolationException("Seat identifier already exists in screen");
        s.setIdentifier(r.identifier().trim());
        s.setScreen(screens.findById(r.screenId()).orElseThrow(() -> new IllegalArgumentException("Screen not found: " + r.screenId())));
        s.setCategory(categories.findById(r.categoryId()).orElseThrow(() -> new IllegalArgumentException("Seat category not found: " + r.categoryId())));
        return seatDto(s);
    }

    public void deleteSeat(Long id) {
        seats.delete(seats.findById(id).orElseThrow(() -> new IllegalArgumentException("Seat not found: " + id)));
    }

    private ScreenResponse screenDto(Screen s) {
        return new ScreenResponse(s.getId(), s.getName(), s.getVenue().getId(), s.getVenue().getName());
    }

    private SeatResponse seatDto(Seat s) {
        return new SeatResponse(s.getId(), s.getIdentifier(), s.getScreen().getId(), s.getCategory().getId(), s.getCategory().getName(), s.getCategory().getPrice());
    }

    private SeatCategoryResponse categoryDto(SeatCategory c) {
        return new SeatCategoryResponse(c.getId(), c.getName(), c.getPrice());
    }
}
