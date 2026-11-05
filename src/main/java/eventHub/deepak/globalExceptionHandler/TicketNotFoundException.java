package eventHub.deepak.globalExceptionHandler;
public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(String reference) { super("Ticket not found: " + reference); }
}
