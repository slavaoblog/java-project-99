package hexlet.code.service;

import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskParamsDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
import hexlet.code.specification.TaskSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskMapper mapper;

    @Autowired
    private TaskSpecification specBuilder;

    public TaskDTO findById(Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id" + id + " not found"));

        return mapper.mapToDto(task);
    }

    public Page<TaskDTO> findAll(TaskParamsDTO params, int page) {
        Specification<Task> specification = specBuilder.build(params);
        Page<Task> tasks = taskRepository.findAll(specification, PageRequest.of(page - 1, 10));
        return tasks.map(mapper::mapToDto);
    }

    public TaskDTO update(TaskDTO taskData, Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id" + id + " not found"));
        mapper.update(taskData, task);
        taskRepository.save(task);
        return mapper.mapToDto(task);
    }

    public TaskDTO create(TaskDTO taskData) {
        var task = mapper.mapToEntity(taskData);

        taskRepository.save(task);
        return mapper.mapToDto(task);
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }
}
