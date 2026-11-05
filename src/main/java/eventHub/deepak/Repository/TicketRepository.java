package eventHub.deepak.repository;

import eventHub.deepak.entity.Ticket;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import eventHub.deepak.enums.TicketStatus;

public interface TicketRepository extends JpaRepository<Ticket,Long> {
    long countByStatus(TicketStatus status);
    @Query("select t.event.id, count(t) from Ticket t where t.event.organizerUser.id=:organizerId group by t.event.id")
    List<Object[]> aggregateCountByOrganizer(@Param("organizerId") Long organizerId);
    Optional<Ticket> findByBookingItem_Id(Long bookingItemId);
    @Query("select t from Ticket t join fetch t.bookingItem i join fetch i.booking b where b.id=:bookingId and b.customer.email=:email order by t.id")
    List<Ticket> findByBookingIdAndCustomerEmail(@Param("bookingId") Long bookingId,@Param("email") String email);
    List<Ticket> findByBookingItem_Booking_Id(Long bookingId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t join fetch t.event e left join fetch e.organizerUser join fetch t.bookingItem i join fetch i.booking b where t.publicReference=:reference")
    Optional<Ticket> findForVerification(@Param("reference") String reference);
}
