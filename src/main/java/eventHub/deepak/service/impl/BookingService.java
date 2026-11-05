package eventHub.deepak.service.impl;

import eventHub.deepak.dto.response.BookingResponse;
import eventHub.deepak.entity.*;
import eventHub.deepak.enums.*;
import eventHub.deepak.globalExceptionHandler.*;
import eventHub.deepak.mapper.BookingMapper;
import eventHub.deepak.repository.*;
import eventHub.deepak.service.reservation.ReservationGrant;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import eventHub.deepak.util.PageableSupport;

@Service
public class BookingService {
    private static final List<BookingStatus> ACTIVE = List.of(BookingStatus.PENDING, BookingStatus.PAYMENT_PENDING);
    private final BookingRepository bookings;
    private final BookingItemRepository items;
    private final ConfirmedSeatClaimRepository claims;
    private final SeatRepository seats;
    private final EventRepository events;
    private final UserRepository users;
    private final SeatReservationService reservations;
    private final BookingMapper mapper;

    public BookingService(BookingRepository bookings, BookingItemRepository items,
                          ConfirmedSeatClaimRepository claims, SeatRepository seats,
                          EventRepository events, UserRepository users,
                          SeatReservationService reservations, BookingMapper mapper) {
        this.bookings = bookings;
        this.items = items;
        this.claims = claims;
        this.seats = seats;
        this.events = events;
        this.users = users;
        this.reservations = reservations;
        this.mapper = mapper;
    }

    @Transactional
    public BookingResponse create(Long eventId, String email, String reservationToken) {
        User customer = customer(email);
        Optional<Booking> prior = bookings
                .findByCustomerIdAndEventIdAndReservationToken(
                        customer.getId(), eventId, reservationToken);
        if (prior.isPresent()) return mapper.toResponse(prior.get());
        Event event = events
                .findById(eventId)
                .orElseThrow(()
                        -> new EventNotFoundException(eventId)
                );
        if (event.getEventStatus() != EventStatus.BOOKING_OPEN) throw new
                SeatUnavailableException("Event is not accepting bookings");

        ReservationGrant grant = reservations
                .activeReservation(eventId, customer.getId(), reservationToken)
                .orElseThrow(ReservationExpiredException::new);

        if (!grant.expiresAt().isAfter(Instant.now())) throw new
                ReservationExpiredException();

        List<Seat> lockedSeats = seats
                .findAllForUpdateByIds(grant.seatIds());
        if (lockedSeats.size() != grant.seatIds().size()
                || lockedSeats.stream().anyMatch(s -> !s.getScreen().getId().equals(event.getScreen().getId())))
            throw new ReservationExpiredException();

        Set<Long> requested = new HashSet<>(grant.seatIds());

        if (claims.findSeatIdsByEventId(eventId).stream().anyMatch(requested::contains)) throw new
                SeatUnavailableException("A seat has already been confirmed for this event");

        Set<Long> pending = items
                .findActiveForEvent(eventId, ACTIVE, Instant.now())
                .stream()
                .map(i -> i.getSeat().getId())
                .collect(java.util.stream.Collectors.toSet());

        if (pending.stream().anyMatch(requested::contains)) throw new
                SeatUnavailableException("A seat is already held by an active booking");
        if (grant.seatIds().stream().anyMatch(id -> !reservations.owns(eventId, id, customer.getId(), reservationToken)))
            throw new ReservationExpiredException();

        Booking booking = Booking.builder().customer(customer).event(event).status(BookingStatus.PENDING)
                .reservationToken(reservationToken).expiresAt(grant.expiresAt()).totalAmount(BigDecimal.ZERO).build();
        BigDecimal total = BigDecimal.ZERO;
        for (Seat seat : lockedSeats) {
            BigDecimal price = seat.getCategory().getPrice();
            BookingItem item = BookingItem.builder().event(event).seat(seat).seatIdentifierSnapshot(seat.getIdentifier()).priceAtBooking(price).build();
            booking.addItem(item);
            total = total.add(price);
        }
        booking.setTotalAmount(total);
        return mapper.toResponse(bookings.saveAndFlush(booking));
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> listMine(String email, Pageable pageable, BookingStatus status) {
        User customer = customer(email);
        Pageable safe = PageableSupport.validate(pageable, Set.of("createdAt"), Sort.by(Sort.Order.desc("createdAt")));
        Page<Booking> result = status == null
                ? bookings.findByCustomerId(customer.getId(), safe)
                : bookings.findByCustomerIdAndStatus(customer.getId(), status, safe);
        return result.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public BookingResponse getMine(Long bookingId, String email) {
        User customer = customer(email);
        return bookings.findByIdAndCustomerId(bookingId, customer.getId()).map(mapper::toResponse).orElseThrow(() -> new BookingNotFoundException(bookingId));
    }

    @Transactional
    public BookingResponse beginPayment(Long bookingId, String email) {
        return transitionOwned(bookingId, email, BookingStatus.PAYMENT_PENDING);
    }

    @Transactional
    public BookingResponse cancel(Long bookingId, String email) {
        User customer = customer(email);
        Booking booking = bookings.findOwnedForUpdate(bookingId, customer.getId()).orElseThrow(() -> new BookingNotFoundException(bookingId));
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.EXPIRED || booking.getStatus() == BookingStatus.FAILED)
            return mapper.toResponse(booking);
        return transition(booking, BookingStatus.CANCELLED);
    }

    @Transactional
    public void cancelReservation(Long eventId, String email, String token) {
        User customer = customer(email);
        Optional<Booking> booking = bookings.findByCustomerIdAndEventIdAndReservationToken(customer.getId(), eventId, token);
        if (booking.isPresent()) {
            Booking owned = bookings.findOwnedForUpdate(booking.get().getId(), customer.getId()).orElseThrow(() -> new BookingNotFoundException(booking.get().getId()));
            if (owned.getStatus() != BookingStatus.CANCELLED && owned.getStatus() != BookingStatus.EXPIRED && owned.getStatus() != BookingStatus.FAILED)
                transition(owned, BookingStatus.CANCELLED);
        } else reservations.releaseOwned(eventId, customer.getId(), token);
    }

    /**
     * Internal lifecycle entry point for the future payment integration.
     */
    @Transactional
    public BookingResponse transitionForPayment(Long bookingId, String email, BookingStatus target) {
        return transitionOwned(bookingId, email, target);
    }

    @Scheduled(fixedDelayString = "${eventhub.booking.expiry-scan-ms:30000}")
    @Transactional
    public void expireOverdueBookings() {
        List<Booking> overdue = bookings.findByStatusInAndExpiresAtBefore(ACTIVE, Instant.now(), PageRequest.of(0, 250));
        for (Booking booking : overdue) transition(booking, BookingStatus.EXPIRED);
    }

    private BookingResponse transitionOwned(Long id, String email, BookingStatus target) {
        User customer = customer(email);
        Booking booking = bookings.findOwnedForUpdate(id, customer.getId()).orElseThrow(() -> new BookingNotFoundException(id));
        return transition(booking, target);
    }

    private BookingResponse transition(Booking booking, BookingStatus target) {
        if (target == BookingStatus.PAYMENT_PENDING && !booking.getExpiresAt().isAfter(Instant.now()))
            target = BookingStatus.EXPIRED;
        BookingStateMachine.requireTransition(booking.getStatus(), target);
        if (booking.getStatus() == target) return mapper.toResponse(booking);
        if (target == BookingStatus.CONFIRMED) confirmClaims(booking);
        if (target == BookingStatus.CANCELLED && booking.getStatus() == BookingStatus.CONFIRMED)
            claims.deleteByBookingItem_IdIn(booking.getItems().stream().map(BookingItem::getId).toList());
        booking.setStatus(target);
        if (target == BookingStatus.CONFIRMED || target == BookingStatus.CANCELLED || target == BookingStatus.FAILED || target == BookingStatus.EXPIRED)
            releaseAfterCommit(booking);
        return mapper.toResponse(bookings.save(booking));
    }

    private void confirmClaims(Booking booking) {
        if (!booking.getExpiresAt()
                .isAfter(Instant.now())) throw new
                ReservationExpiredException();
        if (booking.getEvent()
                .getEventStatus() != EventStatus.BOOKING_OPEN) throw new
                SeatUnavailableException("Event is no longer accepting confirmations");
        List<Long> ids = booking.getItems().stream().map(i -> i.getSeat().getId()).sorted().toList();
        seats.findAllForUpdateByIds(ids);
        if (booking.getItems().stream().anyMatch(i -> !reservations.owns(booking.getEvent().getId(), i.getSeat().getId(), booking.getCustomer().getId(), booking.getReservationToken())))
            throw new ReservationExpiredException();
        List<ConfirmedSeatClaim> seatClaims = booking.getItems().stream().map(i -> ConfirmedSeatClaim.builder()
                .event(booking.getEvent()).seat(i.getSeat()).bookingItem(i).build()).toList();
        claims.saveAllAndFlush(seatClaims);
    }

    private void releaseAfterCommit(Booking booking) {
        Long eventId = booking.getEvent().getId();
        Long customerId = booking.getCustomer().getId();
        String token = booking.getReservationToken();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                reservations.releaseOwned(eventId, customerId, token);
            }
        });
    }

    private User customer(String email) {
        User user = users.findByEmail(email).orElseThrow(() -> new AccessDeniedException("Customer account is unavailable"));
        if (user.getRole() != Role.CUSTOMER || !Boolean.TRUE.equals(user.getIsActive()))
            throw new AccessDeniedException("Only an active customer may access bookings");
        return user;
    }
}
