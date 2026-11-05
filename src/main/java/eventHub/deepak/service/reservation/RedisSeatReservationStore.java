package eventHub.deepak.service.reservation;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class RedisSeatReservationStore implements SeatReservationStore {
    private static final String PREFIX = "eventhub:";
    private static final DefaultRedisScript<Long> COMPARE_AND_DELETE = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end", Long.class);
    private final StringRedisTemplate redis;

    public RedisSeatReservationStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public Optional<ReservationGrant> tryReserve(Long eventId, Long customerId, List<Long> seatIds, Duration ttl) {
        List<Long> ordered = seatIds.stream().distinct().sorted().toList();
        if (ordered.isEmpty()) return Optional.empty();
        String token = UUID.randomUUID().toString();
        String value = ownerValue(customerId, token);
        Instant expiresAt = Instant.now().plus(ttl);
        List<String> acquired = new ArrayList<>();
        try {
            for (Long seatId : ordered) {
                String key = seatKey(eventId, seatId);
                Duration remaining = Duration.between(Instant.now(), expiresAt);
                if (remaining.isZero() || remaining.isNegative()) {
                    acquired.forEach(k -> deleteIfOwner(k, value));
                    return Optional.empty();
                }
                Boolean success = redis.opsForValue().setIfAbsent(key, value, remaining);
                if (!Boolean.TRUE.equals(success)) {
                    acquired.forEach(k -> deleteIfOwner(k, value));
                    return Optional.empty();
                }
                acquired.add(key);
            }
            Duration remaining = Duration.between(Instant.now(), expiresAt);
            if (remaining.isZero() || remaining.isNegative()) {
                acquired.forEach(k -> deleteIfOwner(k, value));
                return Optional.empty();
            }
            redis.opsForValue().set(reservationKey(eventId, customerId, token), encode(ordered, expiresAt), remaining);
            return Optional.of(new ReservationGrant(token, ordered, expiresAt));
        } catch (RuntimeException ex) {
            acquired.forEach(k -> deleteIfOwner(k, value));
            throw ex;
        }
    }

    @Override
    public Optional<ReservationGrant> findReservation(Long eventId, Long customerId, String token) {
        String raw = redis.opsForValue().get(reservationKey(eventId, customerId, token));
        if (raw == null) return Optional.empty();
        try {
            int split = raw.indexOf('|');
            Instant expiry = Instant.ofEpochMilli(Long.parseLong(raw.substring(0, split)));
            List<Long> seats = raw.substring(split + 1).isBlank() ? List.of() : Arrays.stream(raw.substring(split + 1).split(",")).map(Long::parseLong).toList();
            if (!expiry.isAfter(Instant.now()) || seats.isEmpty()) return Optional.empty();
            if (seats.stream().anyMatch(id -> !owns(eventId, id, customerId, token))) return Optional.empty();
            return Optional.of(new ReservationGrant(token, seats, expiry));
        } catch (RuntimeException malformed) {
            return Optional.empty();
        }
    }

    @Override
    public boolean owns(Long eventId, Long seatId, Long customerId, String token) {
        return Objects.equals(redis.opsForValue().get(seatKey(eventId, seatId)), ownerValue(customerId, token));
    }

    @Override
    public Map<Long, SeatLockSnapshot> findLocks(Long eventId, List<Long> seatIds) {
        if (seatIds.isEmpty()) return Map.of();
        List<String> keys = seatIds.stream().map(id -> seatKey(eventId, id)).toList();
        List<String> values = redis.opsForValue().multiGet(keys);
        if (values == null) return Map.of();
        Map<Long, SeatLockSnapshot> locks = new HashMap<>();
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            if (value == null) continue;
            Long millis = redis.getExpire(keys.get(i), TimeUnit.MILLISECONDS);
            if (millis == null || millis <= 0) continue;
            int split = value.indexOf(':');
            if (split < 1) continue;
            try {
                locks.put(seatIds.get(i), new SeatLockSnapshot(Long.parseLong(value.substring(0, split)), value.substring(split + 1), Instant.now().plusMillis(millis)));
            } catch (NumberFormatException ignored) { /* Ignore malformed stale entries. */ }
        }
        return Map.copyOf(locks);
    }

    @Override
    public boolean release(Long eventId, Long customerId, String token) {
        String meta = reservationKey(eventId, customerId, token);
        String raw = redis.opsForValue().get(meta);
        if (raw == null) return false;
        int split = raw.indexOf('|');
        if (split < 0) {
            redis.delete(meta);
            return false;
        }
        String owner = ownerValue(customerId, token);
        String encoded = raw.substring(split + 1);
        if (!encoded.isBlank()) Arrays.stream(encoded.split(",")).map(Long::parseLong)
                .forEach(seatId -> deleteIfOwner(seatKey(eventId, seatId), owner));
        redis.delete(meta);
        return true;
    }

    private void deleteIfOwner(String key, String expected) {
        redis.execute(COMPARE_AND_DELETE, List.of(key), expected);
    }

    private String ownerValue(Long customerId, String token) {
        return customerId + ":" + token;
    }

    private String seatKey(Long eventId, Long seatId) {
        return PREFIX + "seat-lock:event:" + eventId + ":seat:" + seatId;
    }

    private String reservationKey(Long eventId, Long customerId, String token) {
        return PREFIX + "reservation:event:" + eventId + ":customer:" + customerId + ":token:" + token;
    }

    private String encode(List<Long> seatIds, Instant expiry) {
        return expiry.toEpochMilli() + "|" + String.join(",", seatIds.stream().map(String::valueOf).toList());
    }
}
