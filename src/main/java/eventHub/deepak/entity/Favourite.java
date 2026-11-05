package eventHub.deepak.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "favourites", uniqueConstraints = @UniqueConstraint(name = "uk_favourite_customer_event", columnNames = {"customer_id", "event_id"}), indexes = @Index(name = "ix_favourite_customer_created", columnList = "customer_id, created_at"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Favourite extends AuditedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}
