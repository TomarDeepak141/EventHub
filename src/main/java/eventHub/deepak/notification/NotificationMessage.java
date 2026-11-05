package eventHub.deepak.notification;

public record NotificationMessage(NotificationType type, String recipient, Long bookingId, String ticketReference) { }
