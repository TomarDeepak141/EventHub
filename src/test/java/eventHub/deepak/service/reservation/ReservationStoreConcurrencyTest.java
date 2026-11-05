package eventHub.deepak.service.reservation;

import eventHub.deepak.support.InMemorySeatReservationStore;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.List;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class ReservationStoreConcurrencyTest {
    @Test void onlyOneConcurrentCustomerGetsAnEventSeatLock() throws Exception {
        InMemorySeatReservationStore store=new InMemorySeatReservationStore(Clock.systemUTC());
        ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch start=new CountDownLatch(1);
        Callable<Boolean> first=()->{start.await();return store.tryReserve(20L,101L,List.of(9L),Duration.ofMinutes(2)).isPresent();};
        Callable<Boolean> second=()->{start.await();return store.tryReserve(20L,202L,List.of(9L),Duration.ofMinutes(2)).isPresent();};
        Future<Boolean> a=pool.submit(first),b=pool.submit(second);start.countDown();
        assertNotEquals(a.get(3,TimeUnit.SECONDS),b.get(3,TimeUnit.SECONDS));pool.shutdownNow();
    }

    @Test void expiryAndOwnerCheckedReleaseCannotReleaseAnotherCustomersLock() {
        MutableClock clock=new MutableClock(Instant.parse("2026-11-04T00:00:00Z"));
        InMemorySeatReservationStore store=new InMemorySeatReservationStore(clock);
        var grant=store.tryReserve(4L,11L,List.of(5L),Duration.ofSeconds(30)).orElseThrow();
        assertFalse(store.release(4L,22L,grant.token()));
        assertTrue(store.owns(4L,5L,11L,grant.token()));
        clock.advance(Duration.ofSeconds(31));
        assertFalse(store.owns(4L,5L,11L,grant.token()));
        assertTrue(store.tryReserve(4L,22L,List.of(5L),Duration.ofSeconds(30)).isPresent());
    }

    private static final class MutableClock extends Clock {
        private Instant now;MutableClock(Instant now){this.now=now;}void advance(Duration d){now=now.plus(d);}
        @Override public ZoneId getZone(){return ZoneOffset.UTC;}@Override public Clock withZone(ZoneId zone){return this;}@Override public Instant instant(){return now;}
    }
}
