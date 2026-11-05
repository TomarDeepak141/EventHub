package eventHub.deepak.dto.response;

import eventHub.deepak.enums.TicketStatus;
import java.time.Instant;
public record TicketResponse(String ticketReference, String qrPayload, Long bookingId, Long eventId,
                             String seatIdentifier, TicketStatus status, Instant expiresAt) { }
