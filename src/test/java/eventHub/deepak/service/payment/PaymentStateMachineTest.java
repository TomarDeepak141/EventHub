package eventHub.deepak.service.payment;

import eventHub.deepak.enums.PaymentStatus;
import eventHub.deepak.globalExceptionHandler.InvalidBookingTransitionException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentStateMachineTest {
    @Test void onlyAllowsLegalPaymentTransitions() {
        PaymentStateMachine.require(PaymentStatus.INITIATED,PaymentStatus.PROCESSING);
        PaymentStateMachine.require(PaymentStatus.SUCCEEDED,PaymentStatus.REFUND_PENDING);
        assertThrows(InvalidBookingTransitionException.class,()->PaymentStateMachine.require(PaymentStatus.REFUNDED,PaymentStatus.SUCCEEDED));
        assertThrows(InvalidBookingTransitionException.class,()->PaymentStateMachine.require(PaymentStatus.FAILED,PaymentStatus.PROCESSING));
    }
}
