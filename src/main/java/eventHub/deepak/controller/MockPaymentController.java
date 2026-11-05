package eventHub.deepak.controller;

import eventHub.deepak.dto.request.SandboxCompletionRequest;
import eventHub.deepak.dto.response.PaymentResponse;
import eventHub.deepak.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("(demo | dev | development) & !prod")
public class MockPaymentController {
    private final PaymentService payments;

    public MockPaymentController(PaymentService payments) {
        this.payments = payments;
    }

    @PostMapping("/payments/{paymentId}/complete")
    @Operation(summary = "Complete a mock sandbox payment", security = @SecurityRequirement(name = "bearerAuth"))
    public PaymentResponse complete(@PathVariable Long paymentId,
                                    @Valid @RequestBody SandboxCompletionRequest request,
                                    Authentication auth) {
        return payments.complete(paymentId, auth.getName(), request.outcome());
    }
}
