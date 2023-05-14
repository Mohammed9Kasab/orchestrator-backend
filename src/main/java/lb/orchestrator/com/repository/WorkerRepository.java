package lb.orchestrator.com.repository;

import lb.orchestrator.com.domain.Worker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the Worker entity.
 */
@SuppressWarnings("unused")
@Repository
public interface WorkerRepository extends JpaRepository<Worker, Long> {
    Page<Worker> getByUserId(Long userId, Pageable pageable);

    List<Worker> getByUserId(Long userId);

    List<Worker> getByUserIdOrderById(Long userId);
}
