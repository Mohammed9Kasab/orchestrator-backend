package lb.orchestrator.com.repository;

import lb.orchestrator.com.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the Task entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> getByUserId(Long userId, Pageable pageable);

    List<Task> findByUserIdOrderByJobIdAscIdAsc(Long userId);
}
