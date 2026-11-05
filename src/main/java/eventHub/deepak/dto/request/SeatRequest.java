package eventHub.deepak.dto.request;
import jakarta.validation.constraints.*;
public record SeatRequest(@NotBlank @Size(max=30) String identifier,@NotNull @Positive Long screenId,@NotNull @Positive Long categoryId) { }
