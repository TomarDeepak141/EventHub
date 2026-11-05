package eventHub.deepak.repository;
import eventHub.deepak.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Collection;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
public interface SeatRepository extends JpaRepository<Seat,Long> {
    boolean existsByScreenIdAndIdentifierIgnoreCase(Long screenId, String identifier);
    boolean existsByScreenIdAndIdentifierIgnoreCaseAndIdNot(Long screenId, String identifier, Long id);
    List<Seat> findAllByScreenId(Long screenId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id in :ids order by s.id")
    List<Seat> findAllForUpdateByIds(@Param("ids") Collection<Long> ids);
}
