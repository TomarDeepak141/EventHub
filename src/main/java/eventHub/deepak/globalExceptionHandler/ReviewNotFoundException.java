package eventHub.deepak.globalExceptionHandler;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(Long id) { super("Review not found: " + id); }
}
