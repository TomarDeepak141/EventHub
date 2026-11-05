package eventHub.deepak.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats", uniqueConstraints = @UniqueConstraint(name = "uk_seat_screen_identifier", columnNames = {"screen_id", "identifier"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Seat extends AuditedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 30) private String identifier;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "screen_id", nullable = false) private Screen screen;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "category_id", nullable = false) private SeatCategory category;
}
