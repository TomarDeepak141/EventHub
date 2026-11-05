package eventHub.deepak.globalExceptionHandler;

public class EmailAlreadyExistsException
    extends RuntimeException {

    public EmailAlreadyExistsException() {
        super("An account with this email already exists. Sign in or use a different email.");
    }
}
