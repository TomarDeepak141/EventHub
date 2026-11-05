package eventHub.deepak.repository;

import eventHub.deepak.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import eventHub.deepak.enums.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking,Long> {
    Optional<Booking> findByCustomerIdAndEventIdAndReservationToken(Long customerId,Long eventId,String token);
    Optional<Booking> findByIdAndCustomerId(Long id,Long customerId);
    Page<Booking> findByCustomerId(Long customerId,Pageable pageable);
    Page<Booking> findByCustomerIdAndStatus(Long customerId,BookingStatus status,Pageable pageable);
    long countByStatus(BookingStatus status);
    boolean existsByCustomerIdAndEventIdAndStatus(Long customerId,Long eventId,BookingStatus status);
    @Query("select b.event.id, count(b), coalesce(sum(b.totalAmount), 0) from Booking b where b.event.organizerUser.id=:organizerId and b.status=:status group by b.event.id")
    List<Object[]> aggregateByOrganizerAndStatus(@Param("organizerId") Long organizerId,@Param("status") BookingStatus status);
    @Query("select coalesce(sum(b.totalAmount), 0) from Booking b where b.status=:status")
    BigDecimal sumByStatus(@Param("status") BookingStatus status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.id=:id and b.customer.id=:customerId")
    Optional<Booking> findOwnedForUpdate(@Param("id") Long id,@Param("customerId") Long customerId);
    List<Booking> findByStatusInAndExpiresAtBefore(List<BookingStatus> statuses,Instant before,Pageable pageable);
}
