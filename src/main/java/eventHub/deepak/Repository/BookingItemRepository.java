package eventHub.deepak.repository;

import eventHub.deepak.entity.BookingItem;
import eventHub.deepak.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface BookingItemRepository extends JpaRepository<BookingItem,Long> {
    @Query("select i from BookingItem i join fetch i.booking b where i.event.id=:eventId and b.status in :statuses and b.expiresAt>:now")
    List<BookingItem> findActiveForEvent(@Param("eventId") Long eventId,@Param("statuses") List<BookingStatus> statuses,@Param("now") Instant now);
}
