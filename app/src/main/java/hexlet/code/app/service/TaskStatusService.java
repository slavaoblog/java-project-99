package hexlet.code.app.service;

import hexlet.code.app.dto.TaskStatusDTO;
import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.dto.UserDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.repository.TaskStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskStatusService {

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskStatusMapper mapper;

    public TaskStatusDTO findById(Long id) {
        var status = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaskStatus with id" + id + " not found"));

        return mapper.map(status);
    }

    public List<TaskStatusDTO> getAll() {
        var statuses = taskStatusRepository.findAll();

        return statuses.stream()
                .map(mapper::map)
                .toList();
    }

    public TaskStatusDTO create(TaskStatusDTO statusData) {
        var status = mapper.map(statusData);
        taskStatusRepository.save(status);
        return mapper.map(status);
    }

    public TaskStatusDTO update(TaskStatusDTO statusData, Long id) {
        var status = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaskStatus with id" + id + " not found"));

        mapper.update(statusData, status);
        taskStatusRepository.save(status);
        return mapper.map(status);
    }

    public void delete(Long id) {
        taskStatusRepository.deleteById(id);
    }
}
