package eventHub.deepak.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_reviews", uniqueConstraints = @UniqueConstraint(name = "uk_event_review_customer_event", columnNames = {"customer_id", "event_id"}), indexes = @Index(name = "ix_event_review_event_created", columnList = "event_id, created_at"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventReview extends AuditedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private short rating;

    @Column(nullable = false, length = 2000)
    private String comment;
}
