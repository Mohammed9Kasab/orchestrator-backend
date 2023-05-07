package lb.orchestrator.com.service.mapper;

import lb.orchestrator.com.domain.Worker;
import lb.orchestrator.com.service.dto.WorkerDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Worker} and its DTO {@link WorkerDTO}.
 */
@Mapper(componentModel = "spring")
public interface WorkerMapper extends EntityMapper<WorkerDTO, Worker> {}
