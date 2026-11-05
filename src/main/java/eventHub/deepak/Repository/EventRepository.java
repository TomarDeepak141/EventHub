package eventHub.deepak.repository;

import eventHub.deepak.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface EventRepository extends JpaRepository<Event,Long>, JpaSpecificationExecutor<Event> {
    Page<Event> findByOrganizerUserId(Long organizerUserId, Pageable pageable);
    long countByOrganizerUserId(Long organizerUserId);
}
