package eventHub.deepak.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ReservationRequest(@NotEmpty @Size(max=10) List<@NotNull @Positive Long> seatIds) { }
