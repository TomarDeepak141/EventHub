package eventHub.deepak.dto.request;

import eventHub.deepak.enums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    private EventType eventType;

    private String language;

    private String organizer;

    private String imageUrl;

    @NotNull
    private LocalDateTime startTime;

    private Duration duration;

    @NotNull
    private Long venueId;
}