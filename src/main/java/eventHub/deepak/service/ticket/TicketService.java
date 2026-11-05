package eventHub.deepak.service.ticket;

import eventHub.deepak.dto.response.TicketResponse;
import eventHub.deepak.dto.response.TicketVerificationResponse;
import eventHub.deepak.entity.*;
import eventHub.deepak.enums.*;
import eventHub.deepak.globalExceptionHandler.BookingNotFoundException;
import eventHub.deepak.globalExceptionHandler.TicketNotFoundException;
import eventHub.deepak.notification.*;
import eventHub.deepak.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.*;
import java.util.*;

@Service
public class TicketService {
    private static final SecureRandom RANDOM=new SecureRandom();
    private final TicketRepository tickets;
    private final BookingRepository bookings;
    private final UserRepository users;
    private final NotificationService notifications;
    public TicketService(TicketRepository tickets,BookingRepository bookings,UserRepository users,NotificationService notifications) {
        this.tickets=tickets; this.bookings=bookings; this.users=users; this.notifications=notifications;
    }

    @Transactional
    public List<TicketResponse> issueForConfirmedBooking(Long bookingId) {
        Booking booking=bookings.findById(bookingId).orElseThrow(()->new BookingNotFoundException(bookingId));
        if (booking.getStatus()!=BookingStatus.CONFIRMED) throw new IllegalStateException("Tickets can only be issued for a confirmed booking");
        List<TicketResponse> issued=new ArrayList<>();
        for (BookingItem item:booking.getItems()) {
            Optional<Ticket> prior=tickets.findByBookingItem_Id(item.getId());
            Ticket ticket;
            if (prior.isPresent()) ticket=prior.get();
            else {
                ticket=tickets.save(Ticket.builder().publicReference(newReference()).bookingItem(item).event(booking.getEvent())
                        .status(TicketStatus.ACTIVE).expiresAt(expiry(booking.getEvent())).build());
                notifications.afterCommit(new NotificationMessage(NotificationType.TICKET_ISSUED,booking.getCustomer().getEmail(),booking.getId(),ticket.getPublicReference()));
            }
            issued.add(response(ticket));
        }
        return List.copyOf(issued);
    }

    @Transactional(readOnly=true)
    public List<TicketResponse> listMine(Long bookingId,String email) {
        User customer=customer(email);
        if (bookings.findByIdAndCustomerId(bookingId,customer.getId()).isEmpty()) throw new BookingNotFoundException(bookingId);
        return tickets.findByBookingIdAndCustomerEmail(bookingId,email).stream().map(this::response).toList();
    }

    @Transactional
    public int cancelForBooking(Long bookingId) {
        List<Ticket> found=tickets.findByBookingItem_Booking_Id(bookingId);
        int changed=0;
        for (Ticket ticket:found) if (ticket.getStatus()==TicketStatus.ACTIVE || ticket.getStatus()==TicketStatus.USED) {
            ticket.setStatus(TicketStatus.CANCELLED); changed++;
        }
        return changed;
    }

    @Transactional
    public TicketVerificationResponse verify(String reference,String email) {
        User actor=users.findByEmail(email).orElseThrow(()->new AccessDeniedException("Account is unavailable"));
        if (!Boolean.TRUE.equals(actor.getIsActive()) || (actor.getRole()!=Role.ADMIN && actor.getRole()!=Role.ORGANIZER))
            throw new AccessDeniedException("Only an active organizer or admin may verify tickets");
        Ticket ticket=tickets.findForVerification(reference).orElseThrow(()->new TicketNotFoundException(reference));
        if (actor.getRole()!=Role.ADMIN && (ticket.getEvent().getOrganizerUser()==null || !ticket.getEvent().getOrganizerUser().getId().equals(actor.getId())))
            throw new AccessDeniedException("You may verify tickets only for your own events");
        if (ticket.getStatus()==TicketStatus.ACTIVE && !ticket.getExpiresAt().isAfter(Instant.now())) {
            ticket.setStatus(TicketStatus.EXPIRED);
            return verification(ticket,false,"Ticket has expired");
        }
        if (ticket.getStatus()==TicketStatus.ACTIVE) {
            ticket.setStatus(TicketStatus.USED);
            return verification(ticket,true,"Entry approved");
        }
        return switch(ticket.getStatus()) {
            case USED -> verification(ticket,false,"Ticket has already been used");
            case CANCELLED -> verification(ticket,false,"Ticket has been cancelled");
            case EXPIRED -> verification(ticket,false,"Ticket has expired");
            case ACTIVE -> throw new IllegalStateException("Unexpected active ticket state");
        };
    }

    private User customer(String email) {
        User user=users.findByEmail(email).orElseThrow(()->new AccessDeniedException("Customer account is unavailable"));
        if (user.getRole()!=Role.CUSTOMER || !Boolean.TRUE.equals(user.getIsActive())) throw new AccessDeniedException("Only an active customer may access tickets");
        return user;
    }
    private Instant expiry(Event event) {
        Duration duration=event.getDuration()==null?Duration.ofHours(24):event.getDuration();
        return event.getStartTime().plus(duration).toInstant(ZoneOffset.UTC);
    }
    private String newReference() { byte[] raw=new byte[32]; RANDOM.nextBytes(raw); return Base64.getUrlEncoder().withoutPadding().encodeToString(raw); }
    private TicketResponse response(Ticket ticket) {
        return new TicketResponse(ticket.getPublicReference(),"eventhub:ticket:"+ticket.getPublicReference(),ticket.getBookingItem().getBooking().getId(),
                ticket.getEvent().getId(),ticket.getBookingItem().getSeatIdentifierSnapshot(),ticket.getStatus(),ticket.getExpiresAt());
    }
    private TicketVerificationResponse verification(Ticket ticket,boolean admitted,String message) {
        return new TicketVerificationResponse(ticket.getPublicReference(),ticket.getStatus(),admitted,message);
    }
}
