package eventHub.deepak.controller;

import eventHub.deepak.dto.request.PaymentInitiateRequest;
import eventHub.deepak.dto.response.PaymentResponse;
import eventHub.deepak.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class PaymentController {
    private final PaymentService payments;
    public PaymentController(PaymentService payments) { this.payments=payments; }

    @PostMapping("/bookings/{bookingId}/payments")
    @Operation(summary="Initiate a mock payment attempt", security=@SecurityRequirement(name="bearerAuth"))
    public PaymentResponse initiate(@PathVariable Long bookingId,@Valid @RequestBody PaymentInitiateRequest request,Authentication auth) {
        return payments.initiate(bookingId,auth.getName(),request.idempotencyKey());
    }
    @GetMapping("/bookings/{bookingId}/payments")
    @Operation(summary="List payment attempts for an owned booking", security=@SecurityRequirement(name="bearerAuth"))
    public List<PaymentResponse> list(@PathVariable Long bookingId,Authentication auth) { return payments.list(bookingId,auth.getName()); }
    @PostMapping("/payments/{paymentId}/cancel")
    @Operation(summary="Cancel an uncompleted payment attempt", security=@SecurityRequirement(name="bearerAuth"))
    public PaymentResponse cancel(@PathVariable Long paymentId,Authentication auth) { return payments.cancel(paymentId,auth.getName()); }
}
