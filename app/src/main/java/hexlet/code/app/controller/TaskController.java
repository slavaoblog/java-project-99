package hexlet.code.app.controller;

import hexlet.code.app.dto.TaskDTO;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskService taskService;

    @GetMapping(path = "/{id}")
    TaskDTO show(@PathVariable Long id) {
        return taskService.findById(id);
    }

    @GetMapping(path = "")
    ResponseEntity<List<TaskDTO>> index() {
        var tasks = taskService.findAll();
        return ResponseEntity
                .status(HttpStatus.OK)
                .header("X-Total-Count", String.valueOf(tasks.size()))
                .body(tasks);
    }

    @PostMapping(path = "")
    @ResponseStatus(HttpStatus.CREATED)
    TaskDTO create(@RequestBody @Valid TaskDTO taskDTO) {
        return taskService.create(taskDTO);
    }

    @PutMapping(path = "/{id}")
    TaskDTO update(@RequestBody @Valid TaskDTO taskDTO, @PathVariable Long id) {
        return taskService.update(taskDTO, id);
    }

    @DeleteMapping(path = "/{id}")
    void delete(@PathVariable Long id) {
        taskService.delete(id);
    }
}
