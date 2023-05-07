package lb.orchestrator.com.service.mapper;

import lb.orchestrator.com.domain.Job;
import lb.orchestrator.com.domain.Task;
import lb.orchestrator.com.domain.Worker;
import lb.orchestrator.com.service.dto.JobDTO;
import lb.orchestrator.com.service.dto.TaskDTO;
import lb.orchestrator.com.service.dto.WorkerDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Task} and its DTO {@link TaskDTO}.
 */
@Mapper(componentModel = "spring")
public interface TaskMapper extends EntityMapper<TaskDTO, Task> {
//    @Mapping(target = "job", source = "job", qualifiedByName = "jobId")
//    @Mapping(target = "worker", source = "worker", qualifiedByName = "workerId")
    TaskDTO toDto(Task s);

    @Named("jobId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    JobDTO toDtoJobId(Job job);

    @Named("workerId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    WorkerDTO toDtoWorkerId(Worker worker);
}
