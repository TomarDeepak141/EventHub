package eventHub.deepak.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LocalLogNotificationProvider implements NotificationProvider {
    private static final Logger log=LoggerFactory.getLogger(LocalLogNotificationProvider.class);
    @Override public void send(NotificationMessage message) {
        // The local provider deliberately records no credentials and sends no external message.
        // Ticket references are bearer-like admission credentials; keep them and recipients out of logs.
        log.info("Local notification type={} bookingId={}",message.type(),message.bookingId());
    }
}
