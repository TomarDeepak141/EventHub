package eventHub.deepak.entity;

import eventHub.deepak.enums.EventStatus;
import eventHub.deepak.enums.EventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus;

    private String language;
    private String organizer;

    private String imageUrl;

    private LocalDateTime startTime;
    private Duration duration;

    @ManyToOne
    private Venue venue;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
