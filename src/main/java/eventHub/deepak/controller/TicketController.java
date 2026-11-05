package eventHub.deepak.controller;

import eventHub.deepak.dto.request.TicketVerificationRequest;
import eventHub.deepak.dto.response.TicketResponse;
import eventHub.deepak.dto.response.TicketVerificationResponse;
import eventHub.deepak.service.ticket.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class TicketController {
    private final TicketService tickets;
    public TicketController(TicketService tickets) { this.tickets=tickets; }

    @GetMapping("/bookings/{bookingId}/tickets")
    @Operation(summary="List tickets for an owned booking",security=@SecurityRequirement(name="bearerAuth"))
    public List<TicketResponse> mine(@PathVariable Long bookingId,Authentication auth) { return tickets.listMine(bookingId,auth.getName()); }

    @PostMapping("/tickets/verify")
    @Operation(summary="Verify and atomically admit an event ticket",security=@SecurityRequirement(name="bearerAuth"))
    public TicketVerificationResponse verify(@Valid @RequestBody TicketVerificationRequest request,Authentication auth) {
        return tickets.verify(request.ticketReference(),auth.getName());
    }
}
