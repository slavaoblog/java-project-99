package hexlet.code.app.mapper;


import hexlet.code.app.dto.TaskDTO;
import hexlet.code.app.model.Label;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskMapper {

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private JsonNullableMapper jsonNullableMapper;

    @Autowired
    private LabelRepository labelRepository;

    @Mapping(source = "name", target = "title")
    @Mapping(source = "description", target = "content")
    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "taskStatus.slug", target = "status")
    @Mapping(source = "labels", target = "taskLabelIds", qualifiedByName = "labelsToIds")
    public abstract TaskDTO mapToDto(Task model);

    @Mapping(target = "name", source = "title")
    @Mapping(target = "description", source = "content")
    @Mapping(target = "assignee", source = "assigneeId")
    @Mapping(target = "taskStatus", source = "status", qualifiedByName = "slugToTaskStatus")
    @Mapping(target = "labels", source = "taskLabelIds", qualifiedByName = "idsToLabels")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    public abstract Task mapToEntity(TaskDTO data);


    @InheritConfiguration(name = "mapToEntity")
    public abstract void update(TaskDTO data, @MappingTarget Task model);

    @Named("slugToTaskStatus")
    public final TaskStatus toEntity(String status) {
        return taskStatusRepository.findBySlug(status)
                .orElseThrow();
    }

    @Named("labelsToIds")
    public final JsonNullable<Set<Long>> toDto(Set<Label> labels) {
        if (labels != null) {
            return jsonNullableMapper.wrap(labels.stream()
                    .map(Label::getId)
                    .collect(Collectors.toSet()));
        }
        return null;
    }

    @Named("idsToLabels")
    public final Set<Label> toEntity(JsonNullable<Set<Long>> ids) {
        if (ids.isPresent() && ids.get() != null) {
            return ids.get().stream()
                    .map(id -> labelRepository.findById(id).orElse(null))
                    .collect(Collectors.toSet());
        } else if (ids.get() == null) {
            return new HashSet<>();
        }
        return null;
    }
}
