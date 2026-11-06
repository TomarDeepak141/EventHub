package eventHub.deepak.demo;

import eventHub.deepak.enums.Role;
import eventHub.deepak.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;

/** Promotes only an already registered account explicitly named by deployment configuration. */
@Service
public class AdminBootstrapService {
    private final UserRepository users;

    public AdminBootstrapService(UserRepository users) {
        this.users = users;
    }

    @Transactional
    public boolean promoteExistingAccount(String configuredEmail) {
        if (!StringUtils.hasText(configuredEmail)) {
            return false;
        }
        return users.findByEmailIgnoreCase(configuredEmail.trim().toLowerCase(Locale.ROOT))
                .map(user -> {
                    if (user.getRole() != Role.ADMIN) {
                        user.setRole(Role.ADMIN);
                        users.save(user);
                    }
                    return true;
                })
                .orElse(false);
    }
}
