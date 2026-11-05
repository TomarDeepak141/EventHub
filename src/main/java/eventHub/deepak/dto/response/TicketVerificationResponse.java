package eventHub.deepak.dto.response;

import eventHub.deepak.enums.TicketStatus;
public record TicketVerificationResponse(String ticketReference, TicketStatus status, boolean admitted, String message) { }
