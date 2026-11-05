package eventHub.deepak.service.impl;

import eventHub.deepak.enums.BookingStatus;
import eventHub.deepak.globalExceptionHandler.InvalidBookingTransitionException;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class BookingStateMachine {
    private static final Map<BookingStatus, Set<BookingStatus>> ALLOWED = Map.of(
            BookingStatus.PENDING, EnumSet.of(BookingStatus.PAYMENT_PENDING, BookingStatus.FAILED, BookingStatus.CANCELLED, BookingStatus.EXPIRED),
            BookingStatus.PAYMENT_PENDING, EnumSet.of(BookingStatus.CONFIRMED, BookingStatus.FAILED, BookingStatus.CANCELLED, BookingStatus.EXPIRED),
            BookingStatus.CONFIRMED, EnumSet.of(BookingStatus.CANCELLED),
            BookingStatus.FAILED, EnumSet.noneOf(BookingStatus.class),
            BookingStatus.CANCELLED, EnumSet.noneOf(BookingStatus.class),
            BookingStatus.EXPIRED, EnumSet.noneOf(BookingStatus.class));

    private BookingStateMachine() {
    }

    public static void requireTransition(BookingStatus from, BookingStatus to) {
        if (from != to && !ALLOWED.getOrDefault(from, Set.of()).contains(to))
            throw new InvalidBookingTransitionException("Booking cannot transition from " + from + " to " + to);
    }

    public static boolean isActive(BookingStatus status) {
        return status == BookingStatus.PENDING || status == BookingStatus.PAYMENT_PENDING;
    }
}
