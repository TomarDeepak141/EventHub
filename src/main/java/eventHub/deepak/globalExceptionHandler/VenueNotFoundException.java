package eventHub.deepak.globalExceptionHandler;

public class VenueNotFoundException extends RuntimeException{
    public VenueNotFoundException(Long id){
        super("Venue not found with id: "+id);
    }
}