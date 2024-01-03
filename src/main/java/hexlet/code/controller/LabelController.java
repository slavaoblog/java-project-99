package hexlet.code.controller;

import hexlet.code.dto.LabelDTO;
import hexlet.code.repository.LabelRepository;
import hexlet.code.service.LabelService;
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
@RequestMapping("/api/labels")
public class LabelController {

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private LabelService labelService;

    @GetMapping(path = "/{id}")
    LabelDTO show(@PathVariable Long id) {
        return labelService.findById(id);
    }

    @GetMapping(path = "")
    ResponseEntity<List<LabelDTO>> index() {
        var labels = labelService.findAll();
        return ResponseEntity
                .status(HttpStatus.OK)
                .header("X-Total-Count", String.valueOf(labels.size()))
                .body(labels);
    }

    @PostMapping(path = "")
    @ResponseStatus(HttpStatus.CREATED)
    LabelDTO create(@RequestBody @Valid LabelDTO labelDTO) {
        return labelService.create(labelDTO);
    }

    @PutMapping(path = "/{id}")
    LabelDTO update(@RequestBody @Valid LabelDTO labelDTO, @PathVariable Long id) {
        return labelService.update(labelDTO, id);
    }

    @DeleteMapping(path = "/{id}")
    void delete(@PathVariable Long id) {
        labelService.delete(id);
    }
}
