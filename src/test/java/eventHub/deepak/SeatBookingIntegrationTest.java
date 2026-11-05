package eventHub.deepak;

import eventHub.deepak.dto.request.EventRequest;
import eventHub.deepak.entity.*;
import eventHub.deepak.enums.*;
import eventHub.deepak.globalExceptionHandler.SeatUnavailableException;
import eventHub.deepak.globalExceptionHandler.InvalidBookingTransitionException;
import eventHub.deepak.repository.*;
import eventHub.deepak.repository.BookingRepository;
import eventHub.deepak.repository.BookingItemRepository;
import eventHub.deepak.repository.ConfirmedSeatClaimRepository;
import eventHub.deepak.service.impl.BookingService;
import eventHub.deepak.service.impl.SeatReservationService;
import eventHub.deepak.service.payment.PaymentService;
import eventHub.deepak.service.ticket.TicketService;
import eventHub.deepak.service.review.EventReviewService;
import eventHub.deepak.service.favourite.FavouriteService;
import eventHub.deepak.dto.request.ReviewRequest;
import eventHub.deepak.enums.SandboxOutcome;
import eventHub.deepak.service.reservation.SeatReservationStore;
import eventHub.deepak.support.InMemorySeatReservationStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest @ActiveProfiles("demo") @Import(SeatBookingIntegrationTest.StoreConfig.class)
class SeatBookingIntegrationTest {
    @Autowired SeatReservationService reservations; @Autowired BookingService bookings;
    @Autowired VenueRepository venues; @Autowired ScreenRepository screens; @Autowired SeatCategoryRepository categories;
    @Autowired SeatRepository seats; @Autowired UserRepository users; @Autowired EventRepository events;
    @Autowired BookingRepository bookingRepository; @Autowired BookingItemRepository bookingItems;
    @Autowired ConfirmedSeatClaimRepository confirmedClaims; @Autowired PlatformTransactionManager transactionManager;
    @Autowired PaymentService payments;
    @Autowired TicketService tickets;
    @Autowired EventReviewService reviews; @Autowired FavouriteService favourites;

    @Test void mockPaymentSupportsFailureRetryConfirmationAndRefundCancellation() {
        Fixture f=fixture("payment-lifecycle",new BigDecimal("285.00"));
        var reservation=reservations.reserve(f.event.getId(),f.customer.getEmail(),List.of(f.seat.getId()));
        var booking=bookings.create(f.event.getId(),f.customer.getEmail(),reservation.reservationToken());
        var first=payments.initiate(booking.id(),f.customer.getEmail(),"attempt-00000001");
        assertEquals(first.id(),payments.initiate(booking.id(),f.customer.getEmail(),"attempt-00000001").id());
        assertEquals(eventHub.deepak.enums.PaymentStatus.FAILED,payments.complete(first.id(),f.customer.getEmail(),SandboxOutcome.FAIL).status());
        assertEquals(BookingStatus.PAYMENT_PENDING,bookings.getMine(booking.id(),f.customer.getEmail()).status());
        var abandoned=payments.initiate(booking.id(),f.customer.getEmail(),"attempt-00000002");
        assertEquals(eventHub.deepak.enums.PaymentStatus.CANCELLED,payments.cancel(abandoned.id(),f.customer.getEmail()).status());
        assertThrows(eventHub.deepak.globalExceptionHandler.BookingNotFoundException.class,
                ()->payments.complete(abandoned.id(),f.secondCustomer.getEmail(),SandboxOutcome.SUCCEED));
        var retry=payments.initiate(booking.id(),f.customer.getEmail(),"attempt-00000003");
        assertEquals(eventHub.deepak.enums.PaymentStatus.SUCCEEDED,payments.complete(retry.id(),f.customer.getEmail(),SandboxOutcome.SUCCEED).status());
        assertEquals(BookingStatus.CONFIRMED,bookings.getMine(booking.id(),f.customer.getEmail()).status());
        assertEquals(eventHub.deepak.enums.PaymentStatus.SUCCEEDED,payments.complete(retry.id(),f.customer.getEmail(),SandboxOutcome.FAIL).status());
        var issued=tickets.listMine(booking.id(),f.customer.getEmail());
        assertEquals(1,issued.size());
        assertTrue(issued.getFirst().ticketReference().matches("[A-Za-z0-9_-]{32,64}"));
        assertEquals("eventhub:ticket:"+issued.getFirst().ticketReference(),issued.getFirst().qrPayload());
        assertFalse(issued.getFirst().qrPayload().contains(f.customer.getEmail()));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,()->tickets.verify("not-a-ticket-reference-000000000000",f.customer.getEmail()));
        assertEquals(SeatAvailabilityStatus.CONFIRMED_BOOKED,reservations.availability(f.event.getId()).getFirst().status());
        assertEquals(BookingStatus.CANCELLED,payments.cancelBooking(booking.id(),f.customer.getEmail()).status());
        assertEquals(eventHub.deepak.enums.PaymentStatus.REFUNDED,payments.list(booking.id(),f.customer.getEmail()).stream()
                .filter(p->p.id().equals(retry.id())).findFirst().orElseThrow().status());
        assertEquals(SeatAvailabilityStatus.AVAILABLE,reservations.availability(f.event.getId()).getFirst().status());
        assertEquals(eventHub.deepak.enums.TicketStatus.CANCELLED,tickets.listMine(booking.id(),f.customer.getEmail()).getFirst().status());
    }

    @Test void organizerMayAdmitAnOwnedTicketOnlyOnceAndCannotScanOtherOrganizerEvents() {
        Fixture f=fixture("ticket-entry",new BigDecimal("195.00"));
        User organizer=users.save(User.builder().name("Owner").email("owner-"+f.event.getId()+"@eventhub.test").password("encoded").role(Role.ORGANIZER).isActive(true).build());
        f.event.setOrganizerUser(organizer);events.save(f.event);
        var reservation=reservations.reserve(f.event.getId(),f.customer.getEmail(),List.of(f.seat.getId()));
        var booking=bookings.create(f.event.getId(),f.customer.getEmail(),reservation.reservationToken());
        var payment=payments.initiate(booking.id(),f.customer.getEmail(),"ticket-entry-0001");
        payments.complete(payment.id(),f.customer.getEmail(),SandboxOutcome.SUCCEED);
        String reference=tickets.listMine(booking.id(),f.customer.getEmail()).getFirst().ticketReference();
        User wrongOrganizer=users.save(User.builder().name("Other").email("other-"+f.event.getId()+"@eventhub.test").password("encoded").role(Role.ORGANIZER).isActive(true).build());
        assertThrows(org.springframework.security.access.AccessDeniedException.class,()->tickets.verify(reference,wrongOrganizer.getEmail()));
        var admitted=tickets.verify(reference,organizer.getEmail());
        assertTrue(admitted.admitted());assertEquals(eventHub.deepak.enums.TicketStatus.USED,admitted.status());
        var repeated=tickets.verify(reference,organizer.getEmail());
        assertFalse(repeated.admitted());assertEquals("Ticket has already been used",repeated.message());
    }

    @Test void concurrentScansAdmitAValidTicketExactlyOnce() throws Exception {
        Fixture f=fixture("ticket-double-scan",new BigDecimal("205.00"));
        User organizer=users.save(User.builder().name("Scanner").email("scanner-"+f.event.getId()+"@eventhub.test").password("encoded").role(Role.ORGANIZER).isActive(true).build());
        f.event.setOrganizerUser(organizer);events.save(f.event);
        var reservation=reservations.reserve(f.event.getId(),f.customer.getEmail(),List.of(f.seat.getId()));
        var booking=bookings.create(f.event.getId(),f.customer.getEmail(),reservation.reservationToken());
        var payment=payments.initiate(booking.id(),f.customer.getEmail(),"ticket-scan-0001");
        payments.complete(payment.id(),f.customer.getEmail(),SandboxOutcome.SUCCEED);
        String reference=tickets.listMine(booking.id(),f.customer.getEmail()).getFirst().ticketReference();
        ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch start=new CountDownLatch(1);
        Callable<Boolean> scan=()->{start.await();return tickets.verify(reference,organizer.getEmail()).admitted();};
        Future<Boolean> one=pool.submit(scan),two=pool.submit(scan);start.countDown();
        boolean first=one.get(10,TimeUnit.SECONDS),second=two.get(10,TimeUnit.SECONDS);pool.shutdownNow();
        assertNotEquals(first,second,"exactly one concurrent ticket scan must be admitted");
    }

    @Test void reservationBecomesAnIdempotentPricedBookingAndConfirmedSeatStaysEventSpecific() {
        Fixture f=fixture("booking-flow",new BigDecimal("325.00"));
        var reservation=reservations.reserve(f.event.getId(),f.customer.getEmail(),List.of(f.seat.getId()));
        assertEquals(SeatAvailabilityStatus.LOCKED,reservations.availability(f.event.getId()).getFirst().status());
        var booking=bookings.create(f.event.getId(),f.customer.getEmail(),reservation.reservationToken());
        assertEquals(BookingStatus.PENDING,booking.status());
        assertEquals(new BigDecimal("325.00"),booking.items().getFirst().priceAtBooking());
        assertEquals(booking.id(),bookings.create(f.event.getId(),f.customer.getEmail(),reservation.reservationToken()).id());
        bookings.beginPayment(booking.id(),f.customer.getEmail());
        var confirmed=bookings.transitionForPayment(booking.id(),f.customer.getEmail(),BookingStatus.CONFIRMED);
        assertEquals(BookingStatus.CONFIRMED,confirmed.status());
        assertEquals(SeatAvailabilityStatus.CONFIRMED_BOOKED,reservations.availability(f.event.getId()).getFirst().status());
        assertThrows(SeatUnavailableException.class,()->reservations.reserve(f.event.getId(),f.secondCustomer.getEmail(),List.of(f.seat.getId())));
        Event otherScreening=events.save(Event.builder().name("Other screening").eventType(EventType.MOVIE).eventStatus(EventStatus.BOOKING_OPEN)
                .startTime(LocalDateTime.now().plusDays(25)).venue(f.event.getVenue()).screen(f.event.getScreen()).build());
        assertEquals(SeatAvailabilityStatus.AVAILABLE,reservations.availability(otherScreening.getId()).getFirst().status());
        assertNotNull(reservations.reserve(otherScreening.getId(),f.secondCustomer.getEmail(),List.of(f.seat.getId())));
        User third=customer("third-"+f.event.getId()+"@eventhub.test");
        TransactionTemplate tx=new TransactionTemplate(transactionManager);
        assertThrows(DataIntegrityViolationException.class,()->tx.execute(s->{
            Booking competing=bookingRepository.saveAndFlush(Booking.builder().customer(users.getReferenceById(third.getId())).event(events.getReferenceById(f.event.getId()))
                    .status(BookingStatus.PENDING).reservationToken(java.util.UUID.randomUUID().toString()).expiresAt(java.time.Instant.now().plusSeconds(60)).totalAmount(new BigDecimal("325.00")).build());
            BookingItem item=bookingItems.saveAndFlush(BookingItem.builder().booking(competing).event(events.getReferenceById(f.event.getId()))
                    .seat(seats.getReferenceById(f.seat.getId())).seatIdentifierSnapshot("A1").priceAtBooking(new BigDecimal("325.00")).build());
            confirmedClaims.saveAndFlush(ConfirmedSeatClaim.builder().event(events.getReferenceById(f.event.getId())).seat(seats.getReferenceById(f.seat.getId())).bookingItem(item).build());
            return null;
        }));
    }

    @Test void simultaneousCustomersCannotReserveTheSameEventSeat() throws Exception {
        Fixture f=fixture("parallel-reserve",new BigDecimal("100.00"));
        ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch start=new CountDownLatch(1);
        Callable<Boolean> first=()->attempt(start,f.customer,f.event,f.seat);
        Callable<Boolean> second=()->attempt(start,f.secondCustomer,f.event,f.seat);
        Future<Boolean> a=pool.submit(first),b=pool.submit(second);start.countDown();
        boolean resultA=a.get(10,TimeUnit.SECONDS),resultB=b.get(10,TimeUnit.SECONDS);pool.shutdownNow();
        assertNotEquals(resultA,resultB,"exactly one authenticated customer must acquire the event-seat reservation");
    }

    @Test void cancelledBookingCannotBeConfirmedByALaterRetry() {
        Fixture f=fixture("cancel-transition",new BigDecimal("150.00"));
        var reservation=reservations.reserve(f.event.getId(),f.customer.getEmail(),List.of(f.seat.getId()));
        var booking=bookings.create(f.event.getId(),f.customer.getEmail(),reservation.reservationToken());
        assertEquals(BookingStatus.CANCELLED,bookings.cancel(booking.id(),f.customer.getEmail()).status());
        assertThrows(InvalidBookingTransitionException.class,()->bookings.transitionForPayment(booking.id(),f.customer.getEmail(),BookingStatus.CONFIRMED));
    }

    @Test void reviewsRequireCompletedConfirmedBookingAndRemainCustomerOwned() {
        Fixture f=fixture("review-eligibility",new BigDecimal("150.00"));
        assertFalse(reviews.eligibility(f.event.getId(),f.customer.getEmail()).eligible());
        f.event.setStartTime(LocalDateTime.now().minusDays(3));events.save(f.event);
        var reservation=reservations.reserve(f.event.getId(),f.customer.getEmail(),List.of(f.seat.getId()));
        var booking=bookings.create(f.event.getId(),f.customer.getEmail(),reservation.reservationToken());
        var payment=payments.initiate(booking.id(),f.customer.getEmail(),"review-payment-0001");
        payments.complete(payment.id(),f.customer.getEmail(),SandboxOutcome.SUCCEED);
        var review=reviews.create(f.event.getId(),new ReviewRequest((short)5,"  Excellent show  "),f.customer.getEmail());
        assertEquals("Excellent show",review.comment());assertEquals((short)5,review.rating());
        assertThrows(IllegalStateException.class,()->reviews.create(f.event.getId(),new ReviewRequest((short)4,"duplicate"),f.customer.getEmail()));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,()->reviews.update(review.id(),new ReviewRequest((short)1,"not mine"),f.secondCustomer.getEmail()));
    }

    @Test void wishlistIsCustomerScopedAndRepeatedAddIsIdempotent() {
        Fixture f=fixture("wishlist-idempotency",new BigDecimal("150.00"));
        var first=favourites.add(f.event.getId(),f.customer.getEmail());
        var again=favourites.add(f.event.getId(),f.customer.getEmail());
        assertEquals(first.id(),again.id());
        assertEquals(1,favourites.list(f.customer.getEmail(),PageRequest.of(0,20)).getTotalElements());
        assertTrue(favourites.list(f.secondCustomer.getEmail(),PageRequest.of(0,20)).isEmpty());
        favourites.remove(f.event.getId(),f.customer.getEmail());
        assertTrue(favourites.list(f.customer.getEmail(),PageRequest.of(0,20)).isEmpty());
    }

    private boolean attempt(CountDownLatch start,User user,Event event,Seat seat) throws Exception {
        start.await();try{reservations.reserve(event.getId(),user.getEmail(),List.of(seat.getId()));return true;}catch(SeatUnavailableException ex){return false;}
    }

    private Fixture fixture(String suffix,BigDecimal price) {
        User customer=customer("customer-"+suffix+"@eventhub.test");User second=customer("second-"+suffix+"@eventhub.test");
        Venue venue=venues.save(Venue.builder().name("Hall-"+suffix).city("Test City").build());
        Screen screen=screens.save(Screen.builder().name("Screen-"+suffix).venue(venue).build());
        SeatCategory category=categories.save(SeatCategory.builder().name("Cat-"+suffix).price(price).build());
        Seat seat=seats.save(Seat.builder().identifier("A1").screen(screen).category(category).build());
        Event event=events.save(Event.builder().name("Event-"+suffix).eventType(EventType.CONCERT).eventStatus(EventStatus.BOOKING_OPEN)
                .startTime(LocalDateTime.now().plusDays(20)).venue(venue).screen(screen).build());
        return new Fixture(customer,second,event,seat);
    }
    private User customer(String email){return users.save(User.builder().name("Customer").email(email).password("encoded-password").role(Role.CUSTOMER).isActive(true).build());}
    private record Fixture(User customer,User secondCustomer,Event event,Seat seat) { }

    @TestConfiguration static class StoreConfig {
        @Bean @Primary SeatReservationStore testReservationStore(){return new InMemorySeatReservationStore(Clock.systemUTC());}
    }
}
