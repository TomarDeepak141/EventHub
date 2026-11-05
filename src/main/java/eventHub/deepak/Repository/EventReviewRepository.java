package eventHub.deepak.repository;

import eventHub.deepak.entity.EventReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface EventReviewRepository extends JpaRepository<EventReview, Long> {
    Page<EventReview> findByEventId(Long eventId, Pageable pageable);
    Optional<EventReview> findByCustomerIdAndEventId(Long customerId, Long eventId);
    boolean existsByCustomerIdAndEventId(Long customerId, Long eventId);
    long countByEventId(Long eventId);
    long countByEventOrganizerUserId(Long organizerId);
    @Query("select coalesce(avg(r.rating), 0) from EventReview r where r.event.id=:eventId")
    double averageForEvent(@Param("eventId") Long eventId);
    @Query("select r.event.id, count(r), coalesce(avg(r.rating), 0) from EventReview r where r.event.organizerUser.id=:organizerId group by r.event.id")
    List<Object[]> aggregateByOrganizer(@Param("organizerId") Long organizerId);
    @Query("select coalesce(avg(r.rating), 0) from EventReview r")
    double averageRating();
}
