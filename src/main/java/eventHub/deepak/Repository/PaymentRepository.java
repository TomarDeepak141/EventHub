package eventHub.deepak.repository;

import eventHub.deepak.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Optional<Payment> findByBookingIdAndIdempotencyKey(Long bookingId, String key);
    List<Payment> findByBookingIdOrderByCreatedAtDesc(Long bookingId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p join fetch p.booking b where p.id=:id and b.customer.id=:customerId")
    Optional<Payment> findOwnedForUpdate(@Param("id") Long id, @Param("customerId") Long customerId);
}
