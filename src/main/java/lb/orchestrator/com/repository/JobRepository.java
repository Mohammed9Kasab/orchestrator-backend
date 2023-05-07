package lb.orchestrator.com.repository;

import java.util.List;

import lb.orchestrator.com.domain.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Job entity.
 */
@SuppressWarnings("unused")
@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    @Query("select job from Job job where job.user.login = ?#{principal.username}")
    List<Job> findByUserIsCurrentUser();

    Page<Job> getByUserId(Long userId, Pageable pageable);
}
