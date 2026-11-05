package eventHub.deepak.service.impl;

import eventHub.deepak.repository.VenueRepository;
import eventHub.deepak.dto.request.VenueRequest;
import eventHub.deepak.dto.response.VenueResponse;
import eventHub.deepak.entity.Venue;
import eventHub.deepak.globalExceptionHandler.VenueNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import eventHub.deepak.util.PageableSupport;
import org.springframework.data.domain.Sort;
import java.util.Set;

@Service
@Transactional
public class VenueService {
    private final VenueRepository venues;

    public VenueService(VenueRepository venues) {
        this.venues = venues;
    }

    @Transactional(readOnly = true)
    public Page<VenueResponse> getAll(Pageable pageable) {
        Pageable safe=PageableSupport.validate(pageable,Set.of("id","name","city","createdAt","updatedAt"),Sort.by(Sort.Order.asc("name"),Sort.Order.asc("id")));
        return venues.findAll(safe).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public VenueResponse get(Long id) {
        return toResponse(find(id));
    }

    public VenueResponse create(VenueRequest request) {
        Venue venue = new Venue();
        venue.setName(request.name().trim());
        venue.setCity(request.city().trim());
        return toResponse(venues.save(venue));
    }

    public VenueResponse update(Long id, VenueRequest request) {
        Venue venue = find(id);
        venue.setName(request.name().trim());
        venue.setCity(request.city().trim());
        return toResponse(venues.save(venue));
    }

    public void delete(Long id) {
        venues.delete(find(id));
    }

    private Venue find(Long id) {
        return venues.findById(id).orElseThrow(() -> new VenueNotFoundException(id));
    }

    private VenueResponse toResponse(Venue venue) {
        return new VenueResponse(venue.getId(), venue.getName(), venue.getCity());
    }
}
