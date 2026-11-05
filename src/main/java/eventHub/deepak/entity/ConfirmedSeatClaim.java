package eventHub.deepak.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="confirmed_seat_claims",uniqueConstraints={
        @UniqueConstraint(name="uk_confirmed_event_seat",columnNames={"event_id","seat_id"}),
        @UniqueConstraint(name="uk_confirmed_booking_item",columnNames="booking_item_id")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConfirmedSeatClaim {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="event_id",nullable=false) private Event event;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="seat_id",nullable=false) private Seat seat;
    @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="booking_item_id",nullable=false) private BookingItem bookingItem;
}
