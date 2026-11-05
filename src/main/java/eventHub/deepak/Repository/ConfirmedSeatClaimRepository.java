package eventHub.deepak.repository;

import eventHub.deepak.entity.ConfirmedSeatClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ConfirmedSeatClaimRepository extends JpaRepository<ConfirmedSeatClaim,Long> {
    @Query("select c.seat.id from ConfirmedSeatClaim c where c.event.id=:eventId")
    List<Long> findSeatIdsByEventId(@Param("eventId") Long eventId);
    void deleteByBookingItem_IdIn(List<Long> bookingItemIds);
}
