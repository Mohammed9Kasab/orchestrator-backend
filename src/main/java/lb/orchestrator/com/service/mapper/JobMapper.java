package lb.orchestrator.com.service.mapper;

import lb.orchestrator.com.domain.Job;
import lb.orchestrator.com.domain.User;
import lb.orchestrator.com.service.dto.JobDTO;
import lb.orchestrator.com.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Job} and its DTO {@link JobDTO}.
 */
@Mapper(componentModel = "spring")
public interface JobMapper extends EntityMapper<JobDTO, Job> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userId")
    JobDTO toDto(Job s);

    @Named("userId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    UserDTO toDtoUserId(User user);
}
