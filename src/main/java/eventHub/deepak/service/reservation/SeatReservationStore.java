package eventHub.deepak.service.reservation;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SeatReservationStore {
    Optional<ReservationGrant> tryReserve(Long eventId, Long customerId, List<Long> seatIds, Duration ttl);

    Optional<ReservationGrant> findReservation(Long eventId, Long customerId, String token);

    boolean owns(Long eventId, Long seatId, Long customerId, String token);

    Map<Long, SeatLockSnapshot> findLocks(Long eventId, List<Long> seatIds);

    boolean release(Long eventId, Long customerId, String token);
}
