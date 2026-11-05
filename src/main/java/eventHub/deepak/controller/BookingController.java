package eventHub.deepak.controller;

import eventHub.deepak.dto.request.BookingCreateRequest;
import eventHub.deepak.dto.request.ReservationRequest;
import eventHub.deepak.dto.response.*;
import eventHub.deepak.service.impl.BookingService;
import eventHub.deepak.service.impl.SeatReservationService;
import eventHub.deepak.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import eventHub.deepak.enums.BookingStatus;

@RestController
public class BookingController {
    private final BookingService bookings;
    private final SeatReservationService reservations;
    private final PaymentService payments;

    public BookingController(BookingService bookings, SeatReservationService reservations, PaymentService payments) {
        this.bookings = bookings;
        this.reservations = reservations;
        this.payments = payments;
    }

    @GetMapping("/events/{eventId}/availability")
    public List<SeatAvailabilityResponse> availability(@PathVariable Long eventId) {
        return reservations.availability(eventId);
    }

    @PostMapping("/events/{eventId}/reservations")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ReservationResponse> reserve(@PathVariable Long eventId, @Valid @RequestBody ReservationRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservations.reserve(eventId, auth.getName(), request.seatIds()));
    }

    @DeleteMapping("/events/{eventId}/reservations/{token}")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> cancelReservation(@PathVariable Long eventId, @PathVariable String token, Authentication auth) {
        bookings.cancelReservation(eventId, auth.getName(), token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/events/{eventId}/bookings")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<BookingResponse> create(@PathVariable Long eventId, @Valid @RequestBody BookingCreateRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookings.create(eventId, auth.getName(), request.reservationToken()));
    }

    @GetMapping("/bookings")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public Page<BookingResponse> mine(Pageable pageable,@RequestParam(required=false) BookingStatus status, Authentication auth) {
        return bookings.listMine(auth.getName(), pageable,status);
    }

    @GetMapping("/bookings/{id}")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public BookingResponse get(@PathVariable Long id, Authentication auth) {
        return bookings.getMine(id, auth.getName());
    }

    @PostMapping("/bookings/{id}/payment-pending")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public BookingResponse beginPayment(@PathVariable Long id, Authentication auth) {
        return bookings.beginPayment(id, auth.getName());
    }

    @DeleteMapping("/bookings/{id}")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public BookingResponse cancel(@PathVariable Long id, Authentication auth) {
        return payments.cancelBooking(id, auth.getName());
    }
}
