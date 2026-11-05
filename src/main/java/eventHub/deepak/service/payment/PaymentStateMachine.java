package eventHub.deepak.service.payment;

import eventHub.deepak.enums.PaymentStatus;
import eventHub.deepak.globalExceptionHandler.InvalidBookingTransitionException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class PaymentStateMachine {
    private static final Map<PaymentStatus, Set<PaymentStatus>> ALLOWED = Map.of(
            PaymentStatus.INITIATED, EnumSet.of(PaymentStatus.PROCESSING, PaymentStatus.CANCELLED, PaymentStatus.FAILED),
            PaymentStatus.PROCESSING, EnumSet.of(PaymentStatus.SUCCEEDED, PaymentStatus.FAILED, PaymentStatus.CANCELLED),
            PaymentStatus.SUCCEEDED, EnumSet.of(PaymentStatus.REFUND_PENDING),
            PaymentStatus.REFUND_PENDING, EnumSet.of(PaymentStatus.REFUNDED),
            PaymentStatus.FAILED, Set.of(), PaymentStatus.CANCELLED, Set.of(), PaymentStatus.REFUNDED, Set.of());
    private PaymentStateMachine() { }
    public static void require(PaymentStatus from, PaymentStatus to) {
        if (from != to && !ALLOWED.getOrDefault(from, Set.of()).contains(to))
            throw new InvalidBookingTransitionException("Payment cannot transition from " + from + " to " + to);
    }
}
