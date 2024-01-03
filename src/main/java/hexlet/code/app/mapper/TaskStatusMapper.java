package hexlet.code.app.mapper;

import hexlet.code.app.dto.TaskStatusDTO;
import hexlet.code.app.model.TaskStatus;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(uses = {JsonNullableMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskStatusMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    public abstract TaskStatus map(TaskStatusDTO data);
    public abstract TaskStatusDTO map(TaskStatus model);
    @InheritConfiguration
    public abstract void update(TaskStatusDTO data, @MappingTarget TaskStatus model);
}
