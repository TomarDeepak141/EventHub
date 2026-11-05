package eventHub.deepak.repository;

import eventHub.deepak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import eventHub.deepak.enums.Role;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    long countByIsActiveTrue();
    long countByRole(Role role);
}
