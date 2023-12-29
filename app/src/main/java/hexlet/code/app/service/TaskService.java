package hexlet.code.app.service;

import hexlet.code.app.dto.TaskDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskMapper mapper;

    public TaskDTO findById(Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id" + id + " not found"));

        return mapper.mapToDto(task);
    }

    public List<TaskDTO> findAll() {
        var tasks = taskRepository.findAll();

        return tasks.stream()
                .map(mapper::mapToDto)
                .toList();
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
