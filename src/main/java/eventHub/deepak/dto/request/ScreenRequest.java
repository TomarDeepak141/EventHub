package eventHub.deepak.dto.request;
import jakarta.validation.constraints.*;
public record ScreenRequest(@NotBlank @Size(max=120) String name,@NotNull @Positive Long venueId) { }
