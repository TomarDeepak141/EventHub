package eventHub.deepak.entity;

import eventHub.deepak.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name="tickets", uniqueConstraints={
        @UniqueConstraint(name="uk_ticket_public_reference", columnNames="public_reference"),
        @UniqueConstraint(name="uk_ticket_booking_item", columnNames="booking_item_id")},
        indexes={@Index(name="ix_ticket_event_status", columnList="event_id,status"), @Index(name="ix_ticket_expiry", columnList="status,expires_at")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ticket extends AuditedEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="public_reference",nullable=false,length=64) private String publicReference;
    @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="booking_item_id",nullable=false) private BookingItem bookingItem;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="event_id",nullable=false) private Event event;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=16) private TicketStatus status;
    @Column(name="expires_at",nullable=false) private Instant expiresAt;
}
