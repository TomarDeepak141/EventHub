package eventHub.deepak.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "screens", uniqueConstraints = @UniqueConstraint(name = "uk_screen_venue_name", columnNames = {"venue_id", "name"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Screen extends AuditedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String name;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "venue_id", nullable = false) private Venue venue;
    @OneToMany(mappedBy = "screen") private List<Seat> seats;
}
