package lb.orchestrator.com.repository;

import lb.orchestrator.com.domain.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the Job entity.
 */
@SuppressWarnings("unused")
@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    @Query("select job from Job job where job.user.login = ?#{principal.username}")
    List<Job> findByUserIsCurrentUser();

    Page<Job> getByUserId(Long userId, Pageable pageable);

    List<Job> getByUserId(Long userId);
}
