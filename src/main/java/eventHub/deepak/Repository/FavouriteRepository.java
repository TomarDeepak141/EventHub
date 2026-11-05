package eventHub.deepak.repository;

import eventHub.deepak.entity.Favourite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FavouriteRepository extends JpaRepository<Favourite, Long> {
    Page<Favourite> findByCustomerId(Long customerId, Pageable pageable);
    Optional<Favourite> findByCustomerIdAndEventId(Long customerId, Long eventId);
    long deleteByCustomerIdAndEventId(Long customerId, Long eventId);
}
