package eventHub.deepak.service.payment;

import eventHub.deepak.enums.SandboxOutcome;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@Profile("(demo | dev | development) & !prod")
public class MockPaymentProvider implements PaymentProvider {
    @Override public String name() { return "mock"; }
    @Override public ProviderResult complete(String reference, SandboxOutcome outcome) {
        return outcome == SandboxOutcome.SUCCEED
                ? new ProviderResult(true, reference, null)
                : new ProviderResult(false, reference, "MOCK_DECLINED");
    }
    @Override public ProviderResult refund(String reference) { return new ProviderResult(true, "mock-refund-" + UUID.randomUUID(), null); }
}
