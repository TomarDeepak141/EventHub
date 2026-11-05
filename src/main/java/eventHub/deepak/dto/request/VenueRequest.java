package eventHub.deepak.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VenueRequest(@NotBlank @Size(max = 160) String name,
                           @NotBlank @Size(max = 120) String city) { }
