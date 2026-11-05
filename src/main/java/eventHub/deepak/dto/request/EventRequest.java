package eventHub.deepak.dto.request;

import eventHub.deepak.enums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {
    @NotBlank
    @Size(max = 200)
    private String name;

    @Size(max = 5000)
    private String description;

    @NotNull
    private EventType eventType;

    @Size(max = 100)
    private String language;

    @Size(max = 200)
    private String organizer;

    @Size(max = 2000)
    private String imageUrl;

    @NotNull
    private LocalDateTime startTime;

    @Positive
    private Duration duration;

    @NotNull
    private Long venueId;

    @NotNull
    private Long screenId;

    private Long organizerId;
}
