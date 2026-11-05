package eventHub.deepak.support;

import eventHub.deepak.service.reservation.*;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/** Deterministic Redis substitute for tests; methods model atomic SET-NX and owner-checked release. */
public class InMemorySeatReservationStore implements SeatReservationStore {
    private record Key(Long eventId,Long seatId) { }
    private record ReservationKey(Long eventId,Long customerId,String token) { }
    private record Entry(Long customerId,String token,Instant expiresAt) { }
    private final Clock clock;
    private final Map<Key,Entry> locks=new HashMap<>();
    private final Map<ReservationKey,ReservationGrant> reservations=new HashMap<>();
    public InMemorySeatReservationStore(Clock clock) { this.clock=clock; }

    @Override public synchronized Optional<ReservationGrant> tryReserve(Long eventId,Long customerId,List<Long> seatIds,Duration ttl) {
        reap();List<Long> ordered=seatIds.stream().distinct().sorted().toList();
        if(ordered.isEmpty()||ordered.stream().anyMatch(id->locks.containsKey(new Key(eventId,id)))) return Optional.empty();
        String token=UUID.randomUUID().toString();Instant expiry=clock.instant().plus(ttl);
        ordered.forEach(id->locks.put(new Key(eventId,id),new Entry(customerId,token,expiry)));
        ReservationGrant grant=new ReservationGrant(token,ordered,expiry);
        reservations.put(new ReservationKey(eventId,customerId,token),grant);
        return Optional.of(grant);
    }
    @Override public synchronized Optional<ReservationGrant> findReservation(Long eventId,Long customerId,String token) {
        reap();ReservationGrant grant=reservations.get(new ReservationKey(eventId,customerId,token));
        if(grant==null||grant.seatIds().stream().anyMatch(id->!owns(eventId,id,customerId,token))) return Optional.empty();
        return Optional.of(grant);
    }
    @Override public synchronized boolean owns(Long eventId,Long seatId,Long customerId,String token) {
        reap();Entry entry=locks.get(new Key(eventId,seatId));return entry!=null&&entry.customerId().equals(customerId)&&entry.token().equals(token);
    }
    @Override public synchronized Map<Long,SeatLockSnapshot> findLocks(Long eventId,List<Long> seatIds) {
        reap();Map<Long,SeatLockSnapshot> result=new HashMap<>();
        for(Long id:seatIds){Entry e=locks.get(new Key(eventId,id));if(e!=null)result.put(id,new SeatLockSnapshot(e.customerId(),e.token(),e.expiresAt()));}
        return Map.copyOf(result);
    }
    @Override public synchronized boolean release(Long eventId,Long customerId,String token) {
        reap();ReservationGrant grant=reservations.remove(new ReservationKey(eventId,customerId,token));if(grant==null)return false;
        for(Long seatId:grant.seatIds()){Key key=new Key(eventId,seatId);Entry e=locks.get(key);if(e!=null&&e.customerId().equals(customerId)&&e.token().equals(token))locks.remove(key);}
        return true;
    }
    private void reap(){Instant now=clock.instant();locks.entrySet().removeIf(e->!e.getValue().expiresAt().isAfter(now));reservations.entrySet().removeIf(e->!e.getValue().expiresAt().isAfter(now));}
}
