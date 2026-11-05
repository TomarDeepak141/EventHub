package eventHub.deepak.service.impl;

import eventHub.deepak.enums.BookingStatus;
import eventHub.deepak.globalExceptionHandler.InvalidBookingTransitionException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookingStateMachineTest {
    @Test void allowsOnlyDeclaredPaymentAndTerminalTransitions() {
        assertDoesNotThrow(()->BookingStateMachine.requireTransition(BookingStatus.PENDING,BookingStatus.PAYMENT_PENDING));
        assertDoesNotThrow(()->BookingStateMachine.requireTransition(BookingStatus.PAYMENT_PENDING,BookingStatus.CONFIRMED));
        assertThrows(InvalidBookingTransitionException.class,()->BookingStateMachine.requireTransition(BookingStatus.CANCELLED,BookingStatus.CONFIRMED));
        assertDoesNotThrow(()->BookingStateMachine.requireTransition(BookingStatus.CONFIRMED,BookingStatus.CANCELLED));
        assertThrows(InvalidBookingTransitionException.class,()->BookingStateMachine.requireTransition(BookingStatus.CANCELLED,BookingStatus.CONFIRMED));
    }
}
