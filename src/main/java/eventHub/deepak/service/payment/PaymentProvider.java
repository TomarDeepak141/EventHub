package eventHub.deepak.service.payment;

import eventHub.deepak.enums.SandboxOutcome;
public interface PaymentProvider {
    String name();
    default boolean isEnabled() { return true; }
    ProviderResult complete(String paymentReference, SandboxOutcome requestedOutcome);
    ProviderResult refund(String paymentReference);
    record ProviderResult(boolean successful, String reference, String failureCode) { }
}
