package eventHub.deepak.service.interfaces;

import eventHub.deepak.entity.User;
import eventHub.deepak.enums.Role;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String generateToken(User user);

    String extractEmail(String token);

    Long extractUserId(String token);

    Role extractRole(String token);

    boolean validateToken(String token, UserDetails userDetails);
}
