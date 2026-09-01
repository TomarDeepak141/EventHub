package eventHub.deepak.dto.response;

import eventHub.deepak.enums.EventStatus;
import eventHub.deepak.enums.EventType;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Data
public class EventResponse {

    private Long id;

    private String name;

    private String description;

    private EventType eventType;

    private EventStatus eventStatus;

    private String language;

    private String organizer;

    private String imageUrl;

    private LocalDateTime startTime;

    private Duration duration;

    private String venueName;

    private String city;
}
