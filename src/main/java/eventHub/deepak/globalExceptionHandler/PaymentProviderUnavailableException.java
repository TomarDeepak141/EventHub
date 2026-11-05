package eventHub.deepak.globalExceptionHandler;

public class PaymentProviderUnavailableException extends RuntimeException {
    public PaymentProviderUnavailableException() {
        super("Payment processing is not configured for this environment");
    }
}
