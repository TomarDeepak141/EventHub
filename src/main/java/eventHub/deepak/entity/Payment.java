package eventHub.deepak.entity;

import eventHub.deepak.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name="payments", indexes={@Index(name="ix_payment_booking_created", columnList="booking_id,created_at")},
       uniqueConstraints=@UniqueConstraint(name="uk_payment_booking_idempotency", columnNames={"booking_id","idempotency_key"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment extends AuditedEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="booking_id",nullable=false) private Booking booking;
    @Column(name="idempotency_key",nullable=false,length=80) private String idempotencyKey;
    @Column(nullable=false,length=32) @Builder.Default private String provider="mock";
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=24) private PaymentStatus status;
    @Column(nullable=false,precision=12,scale=2) private BigDecimal amount;
    @Column(name="provider_reference",length=100) private String providerReference;
    @Column(name="failure_code",length=80) private String failureCode;
}
