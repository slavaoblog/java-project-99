package hexlet.code.controller;


import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.service.TaskStatusService;
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
@RequestMapping("/api/task_statuses")
public class TaskStatusController {

    @Autowired
    private TaskStatusService taskStatusService;

    @GetMapping(path = "/{id}")
    TaskStatusDTO show(@PathVariable Long id) {
        return taskStatusService.findById(id);
    }

    @GetMapping(path = "")
    ResponseEntity<List<TaskStatusDTO>> index() {
        var statuses = taskStatusService.getAll();
        return ResponseEntity
                .status(HttpStatus.OK)
                .header("X-Total-Count", String.valueOf(statuses.size()))
                .body(statuses);
    }

    @PostMapping(path = "")
    @ResponseStatus(HttpStatus.CREATED)
    TaskStatusDTO create(@RequestBody @Valid TaskStatusDTO taskStatusDTO) {
        return taskStatusService.create(taskStatusDTO);
    }

    @PutMapping(path = "/{id}")
    TaskStatusDTO update(@RequestBody @Valid TaskStatusDTO taskStatusDTO, @PathVariable Long id) {
        return taskStatusService.update(taskStatusDTO, id);
    }

    @DeleteMapping(path = "/{id}")
    void delete(@PathVariable Long id) {
        taskStatusService.delete(id);
    }
}
