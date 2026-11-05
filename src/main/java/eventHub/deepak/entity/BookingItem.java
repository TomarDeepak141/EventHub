package eventHub.deepak.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name="booking_items",uniqueConstraints=@UniqueConstraint(name="uk_booking_item_seat",columnNames={"booking_id","seat_id"}),indexes={
        @Index(name="ix_booking_item_event_seat",columnList="event_id, seat_id")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingItem extends AuditedEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="booking_id",nullable=false) private Booking booking;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="event_id",nullable=false) private Event event;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="seat_id",nullable=false) private Seat seat;
    @Column(name="seat_identifier_snapshot",nullable=false,length=30) private String seatIdentifierSnapshot;
    @Column(name="price_at_booking",nullable=false,precision=12,scale=2) private BigDecimal priceAtBooking;
}
