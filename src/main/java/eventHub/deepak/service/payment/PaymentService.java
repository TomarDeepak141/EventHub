package eventHub.deepak.service.payment;

import eventHub.deepak.dto.response.PaymentResponse;
import eventHub.deepak.dto.response.BookingResponse;
import eventHub.deepak.entity.*;
import eventHub.deepak.enums.*;
import eventHub.deepak.globalExceptionHandler.BookingNotFoundException;
import eventHub.deepak.globalExceptionHandler.PaymentProviderUnavailableException;
import eventHub.deepak.repository.*;
import eventHub.deepak.service.impl.BookingService;
import eventHub.deepak.service.ticket.TicketService;
import eventHub.deepak.notification.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PaymentService {
    private final PaymentRepository payments;
    private final BookingRepository bookings;
    private final UserRepository users;
    private final ConfirmedSeatClaimRepository claims;
    private final BookingService bookingService;
    private final PaymentProvider provider;
    private final TicketService tickets;
    private final NotificationService notifications;

    public PaymentService(PaymentRepository payments, BookingRepository bookings, UserRepository users,
                          ConfirmedSeatClaimRepository claims, BookingService bookingService, PaymentProvider provider,
                          TicketService tickets, NotificationService notifications) {
        this.payments=payments; this.bookings=bookings; this.users=users; this.claims=claims;
        this.bookingService=bookingService; this.provider=provider; this.tickets=tickets; this.notifications=notifications;
    }

    @Transactional
    public PaymentResponse initiate(Long bookingId, String email, String idempotencyKey) {
        User customer=customer(email);
        Booking booking=bookings.findOwnedForUpdate(bookingId, customer.getId()).orElseThrow(()->new BookingNotFoundException(bookingId));
        if (!provider.isEnabled()) throw new PaymentProviderUnavailableException();
        var prior=payments.findByBookingIdAndIdempotencyKey(bookingId,idempotencyKey);
        if (prior.isPresent()) return response(prior.get());
        if (booking.getStatus()!=BookingStatus.PENDING && booking.getStatus()!=BookingStatus.PAYMENT_PENDING)
            throw new IllegalStateException("Payments can only be initiated for a pending booking");
        if (!booking.getExpiresAt().isAfter(java.time.Instant.now())) throw new IllegalStateException("Booking has expired");
        if (booking.getStatus()==BookingStatus.PENDING) bookingService.beginPayment(bookingId,email);
        Payment payment=Payment.builder().booking(booking).idempotencyKey(idempotencyKey).provider(provider.name())
                .status(PaymentStatus.INITIATED).amount(booking.getTotalAmount()).build();
        return response(payments.save(payment));
    }

    @Transactional(readOnly=true)
    public List<PaymentResponse> list(Long bookingId,String email) {
        User customer=customer(email);
        if (bookings.findByIdAndCustomerId(bookingId,customer.getId()).isEmpty()) throw new BookingNotFoundException(bookingId);
        return payments.findByBookingIdOrderByCreatedAtDesc(bookingId).stream().map(this::response).toList();
    }

    @Transactional
    public PaymentResponse complete(Long paymentId,String email,SandboxOutcome outcome) {
        if (!provider.isEnabled()) throw new PaymentProviderUnavailableException();
        Payment payment=owned(paymentId,email);
        if (payment.getStatus()==PaymentStatus.SUCCEEDED || payment.getStatus()==PaymentStatus.FAILED) return response(payment);
        PaymentStateMachine.require(payment.getStatus(),PaymentStatus.PROCESSING);
        payment.setStatus(PaymentStatus.PROCESSING);
        PaymentProvider.ProviderResult result=provider.complete("mock-payment-"+payment.getId(),outcome);
        payment.setProviderReference(result.reference());
        if (result.successful()) {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            var confirmed=bookingService.transitionForPayment(payment.getBooking().getId(),email,BookingStatus.CONFIRMED);
            if (confirmed.status()==BookingStatus.CONFIRMED) {
                tickets.issueForConfirmedBooking(payment.getBooking().getId());
                notifications.afterCommit(new NotificationMessage(NotificationType.BOOKING_CONFIRMED,
                        payment.getBooking().getCustomer().getEmail(),payment.getBooking().getId(),null));
            }
        } else { payment.setStatus(PaymentStatus.FAILED); payment.setFailureCode(result.failureCode()); }
        return response(payment);
    }

    @Transactional
    public PaymentResponse cancel(Long paymentId,String email) {
        Payment payment=owned(paymentId,email);
        if (payment.getStatus()==PaymentStatus.CANCELLED) return response(payment);
        PaymentStateMachine.require(payment.getStatus(),PaymentStatus.CANCELLED);
        payment.setStatus(PaymentStatus.CANCELLED);
        return response(payment);
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId,String email) {
        User customer=customer(email);
        Booking booking=bookings.findOwnedForUpdate(bookingId,customer.getId()).orElseThrow(()->new BookingNotFoundException(bookingId));
        boolean wasAlreadyCancelled=booking.getStatus()==BookingStatus.CANCELLED;
        List<Payment> related=payments.findByBookingIdOrderByCreatedAtDesc(bookingId);
        Payment successful=related.stream().filter(p->p.getStatus()==PaymentStatus.SUCCEEDED).findFirst().orElse(null);
        if (successful!=null) {
            PaymentStateMachine.require(successful.getStatus(),PaymentStatus.REFUND_PENDING);
            successful.setStatus(PaymentStatus.REFUND_PENDING);
            PaymentProvider.ProviderResult refund=provider.refund(successful.getProviderReference());
            if (!refund.successful()) throw new IllegalStateException("Mock refund could not be completed");
            successful.setProviderReference(refund.reference());
            successful.setStatus(PaymentStatus.REFUNDED);
        }
        for (Payment payment:related) if (payment.getStatus()==PaymentStatus.INITIATED || payment.getStatus()==PaymentStatus.PROCESSING) payment.setStatus(PaymentStatus.CANCELLED);
        tickets.cancelForBooking(bookingId);
        BookingResponse cancelled=bookingService.cancel(bookingId,email);
        if (!wasAlreadyCancelled && cancelled.status()==BookingStatus.CANCELLED)
            notifications.afterCommit(new NotificationMessage(NotificationType.BOOKING_CANCELLED_OR_REFUNDED,customer.getEmail(),bookingId,null));
        return cancelled;
    }

    private Payment owned(Long id,String email) {
        User customer=customer(email);
        return payments.findOwnedForUpdate(id,customer.getId()).orElseThrow(()->new BookingNotFoundException(id));
    }
    private User customer(String email) {
        User user=users.findByEmail(email).orElseThrow(()->new AccessDeniedException("Customer account is unavailable"));
        if (user.getRole()!=Role.CUSTOMER || !Boolean.TRUE.equals(user.getIsActive())) throw new AccessDeniedException("Only an active customer may access payments");
        return user;
    }
    private PaymentResponse response(Payment p) { return new PaymentResponse(p.getId(),p.getBooking().getId(),p.getProvider(),p.getStatus(),p.getAmount(),p.getProviderReference(),p.getFailureCode(),p.getCreatedAt(),p.getUpdatedAt()); }
}
