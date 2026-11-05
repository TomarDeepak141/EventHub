package eventHub.deepak.repository;
import eventHub.deepak.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ScreenRepository extends JpaRepository<Screen,Long> {
    boolean existsByVenueIdAndNameIgnoreCase(Long venueId,String name);
    boolean existsByVenueIdAndNameIgnoreCaseAndIdNot(Long venueId,String name,Long id);
}
