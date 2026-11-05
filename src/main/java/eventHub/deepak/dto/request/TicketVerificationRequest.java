package eventHub.deepak.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
public record TicketVerificationRequest(@NotBlank @Pattern(regexp="[A-Za-z0-9_-]{32,64}") String ticketReference) { }
