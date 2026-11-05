package eventHub.deepak.service.payment;

import eventHub.deepak.enums.SandboxOutcome;
import eventHub.deepak.globalExceptionHandler.PaymentProviderUnavailableException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("(!demo & !dev & !development) | prod")
public class UnavailablePaymentProvider implements PaymentProvider {
    @Override public String name() { return "unconfigured"; }
    @Override public boolean isEnabled() { return false; }
    @Override public ProviderResult complete(String paymentReference, SandboxOutcome requestedOutcome) {
        throw new PaymentProviderUnavailableException();
    }
    @Override public ProviderResult refund(String paymentReference) {
        throw new PaymentProviderUnavailableException();
    }
}
