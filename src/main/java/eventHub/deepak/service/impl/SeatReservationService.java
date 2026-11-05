package eventHub.deepak.service.impl;

import eventHub.deepak.config.ReservationProperties;
import eventHub.deepak.dto.response.SeatAvailabilityResponse;
import eventHub.deepak.dto.response.ReservationResponse;
import eventHub.deepak.entity.*;
import eventHub.deepak.enums.*;
import eventHub.deepak.globalExceptionHandler.EventNotFoundException;
import eventHub.deepak.globalExceptionHandler.SeatUnavailableException;
import eventHub.deepak.repository.*;
import eventHub.deepak.service.reservation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatReservationService {
    private static final List<BookingStatus> ACTIVE = List.of(BookingStatus.PENDING, BookingStatus.PAYMENT_PENDING);
    private final EventRepository events;
    private final SeatRepository seats;
    private final UserRepository users;
    private final BookingItemRepository items;
    private final ConfirmedSeatClaimRepository claims;
    private final SeatReservationStore store;
    private final ReservationProperties properties;

    public SeatReservationService(EventRepository events, SeatRepository seats, UserRepository users, BookingItemRepository items,
                                  ConfirmedSeatClaimRepository claims, SeatReservationStore store, ReservationProperties properties) {
        this.events = events;
        this.seats = seats;
        this.users = users;
        this.items = items;
        this.claims = claims;
        this.store = store;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public List<SeatAvailabilityResponse> availability(Long eventId) {
        Event event = events.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        List<Seat> eventSeats = seats.findAllByScreenId(event.getScreen().getId());
        List<Long> ids = eventSeats.stream().map(Seat::getId).toList();
        Set<Long> confirmed = new HashSet<>(claims.findSeatIdsByEventId(eventId));
        Map<Long, Instant> active = items.findActiveForEvent(eventId, ACTIVE, Instant.now()).stream()
                .collect(Collectors.toMap(i -> i.getSeat().getId(), i -> i.getBooking().getExpiresAt(), (a, b) -> a.isBefore(b) ? a : b));
        Map<Long, SeatLockSnapshot> locks = store.findLocks(eventId, ids);
        return eventSeats.stream().map(seat -> {
            Long seatId = seat.getId();
            if (confirmed.contains(seatId)) return response(seat, SeatAvailabilityStatus.CONFIRMED_BOOKED, null);
            Instant pendingExpiry = active.get(seatId);
            if (pendingExpiry != null) return response(seat, SeatAvailabilityStatus.LOCKED, pendingExpiry);
            SeatLockSnapshot lock = locks.get(seatId);
            return response(seat, lock == null ? SeatAvailabilityStatus.AVAILABLE : SeatAvailabilityStatus.LOCKED, lock == null ? null : lock.expiresAt());
        }).toList();
    }

    @Transactional
    public ReservationResponse reserve(Long eventId, String email, List<Long> requestedSeatIds) {
        User customer = customer(email);
        Event event = events.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        if (event.getEventStatus() != EventStatus.BOOKING_OPEN)
            throw new SeatUnavailableException("Event is not accepting reservations");
        if (requestedSeatIds == null || requestedSeatIds.isEmpty() || requestedSeatIds.size() > 10)
            throw new IllegalArgumentException("Choose between 1 and 10 seats");
        List<Long> ids = requestedSeatIds.stream().distinct().sorted().toList();
        if (ids.size() != requestedSeatIds.size())
            throw new IllegalArgumentException("Seat ids must not contain duplicates");
        List<Seat> lockedSeats = seats.findAllForUpdateByIds(ids);
        if (lockedSeats.size() != ids.size() || lockedSeats.stream().anyMatch(s -> !s.getScreen().getId().equals(event.getScreen().getId())))
            throw new IllegalArgumentException("All seats must belong to the event screen");
        assertAvailable(eventId, ids);
        Optional<ReservationGrant> grant = store.tryReserve(eventId, customer.getId(), ids, properties.ttl());
        if (grant.isEmpty()) throw new SeatUnavailableException("One or more seats are already reserved");
        ReservationGrant reservation = grant.get();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) store.release(eventId, customer.getId(), reservation.token());
            }
        });
        return new ReservationResponse(reservation.token(), eventId, reservation.seatIds(), reservation.expiresAt());
    }

    @Transactional
    public void release(Long eventId, String email, String token) {
        User customer = customer(email);
        store.release(eventId, customer.getId(), token);
    }

    public Optional<ReservationGrant> activeReservation(Long eventId, Long customerId, String token) {
        return store.findReservation(eventId, customerId, token);
    }

    public boolean owns(Long eventId, Long seatId, Long customerId, String token) {
        return store.owns(eventId, seatId, customerId, token);
    }

    public void releaseOwned(Long eventId, Long customerId, String token) {
        store.release(eventId, customerId, token);
    }

    private void assertAvailable(Long eventId, List<Long> ids) {
        Set<Long> confirmed = new HashSet<>(claims.findSeatIdsByEventId(eventId));
        Set<Long> pending = items.findActiveForEvent(eventId, ACTIVE, Instant.now()).stream().map(i -> i.getSeat().getId()).collect(Collectors.toSet());
        Set<Long> locked = store.findLocks(eventId, ids).keySet();
        if (ids.stream().anyMatch(id -> confirmed.contains(id) || pending.contains(id) || locked.contains(id)))
            throw new SeatUnavailableException("One or more seats are no longer available");
    }

    private User customer(String email) {
        User user = users.findByEmail(email).orElseThrow(() -> new AccessDeniedException("Customer account is unavailable"));
        if (user.getRole() != Role.CUSTOMER || !Boolean.TRUE.equals(user.getIsActive()))
            throw new AccessDeniedException("Only an active customer may reserve seats");
        return user;
    }

    private SeatAvailabilityResponse response(Seat seat, SeatAvailabilityStatus status, Instant expiry) {
        SeatCategory category = seat.getCategory();
        return new SeatAvailabilityResponse(seat.getId(), seat.getIdentifier(), category.getId(), category.getName(), category.getPrice(), status, expiry);
    }
}
