package eventHub.deepak.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties(prefix="eventhub.reservation")
public record ReservationProperties(Duration ttl) {
    public ReservationProperties {
        if (ttl == null || ttl.compareTo(Duration.ofSeconds(30)) < 0 || ttl.compareTo(Duration.ofMinutes(15)) > 0)
            throw new IllegalArgumentException("Reservation TTL must be between 30 seconds and 15 minutes");
    }
}
