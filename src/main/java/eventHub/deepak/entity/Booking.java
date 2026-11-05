package eventHub.deepak.entity;

import eventHub.deepak.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="bookings", uniqueConstraints=@UniqueConstraint(name="uk_booking_customer_event_token", columnNames={"customer_id","event_id","reservation_token"}), indexes={
        @Index(name="ix_booking_customer_created", columnList="customer_id, created_at"),
        @Index(name="ix_booking_event_state_expiry", columnList="event_id, status, expires_at")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking extends AuditedEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="customer_id",nullable=false) private User customer;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="event_id",nullable=false) private Event event;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=24) private BookingStatus status;
    @Column(name="reservation_token",nullable=false,length=36) private String reservationToken;
    @Column(name="expires_at",nullable=false) private Instant expiresAt;
    @Column(name="total_amount",nullable=false,precision=12,scale=2) private BigDecimal totalAmount;
    @OneToMany(mappedBy="booking",cascade=CascadeType.ALL,orphanRemoval=true)
    @Builder.Default private List<BookingItem> items=new ArrayList<>();

    public void addItem(BookingItem item) { items.add(item); item.setBooking(this); }
}
