package eventHub.deepak.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
public record PaymentInitiateRequest(@NotBlank @Pattern(regexp="[A-Za-z0-9._:-]{8,80}") String idempotencyKey) { }
