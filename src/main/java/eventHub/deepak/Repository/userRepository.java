package eventHub.deepak.Repository;

import eventHub.deepak.entity.user;
import org.springframework.data.jpa.repository.JpaRepository;

public interface userRepository extends JpaRepository<user, Long> {
    boolean existsByEmail(String email);
}
