package eventHub.deepak.globalExceptionHandler;

public class EventNotFoundException extends RuntimeException{
    public EventNotFoundException(Long id){
        super("Event Not Found" +id);
    }
}
