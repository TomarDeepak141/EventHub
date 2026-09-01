package eventHub.deepak.Repository;

import eventHub.deepak.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository
        extends JpaRepository<Venue, Long> {
}
