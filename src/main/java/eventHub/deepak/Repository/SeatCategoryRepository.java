package eventHub.deepak.repository;
import eventHub.deepak.entity.SeatCategory;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SeatCategoryRepository extends JpaRepository<SeatCategory,Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name,Long id);
}
