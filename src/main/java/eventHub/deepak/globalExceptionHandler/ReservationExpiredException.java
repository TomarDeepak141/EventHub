package eventHub.deepak.globalExceptionHandler;
public class ReservationExpiredException extends RuntimeException { public ReservationExpiredException(){super("Reservation is missing, expired, or no longer owned by this account");} }
