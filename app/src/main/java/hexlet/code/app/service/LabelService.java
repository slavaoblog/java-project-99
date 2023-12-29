package hexlet.code.app.service;

import hexlet.code.app.dto.LabelDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.LabelMapper;
import hexlet.code.app.repository.LabelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LabelService {
    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private LabelMapper mapper;

    public LabelDTO findById(Long id) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label with id" + id + " not found"));

        return mapper.map(label);
    }

    public List<LabelDTO> findAll() {
        var labels = labelRepository.findAll();

        return labels.stream()
                .map(mapper::map)
                .toList();
    }

    public LabelDTO update(LabelDTO labelData, Long id) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label with id" + id + " not found"));
        mapper.update(labelData, label);
        labelRepository.save(label);
        return mapper.map(label);
    }

    public LabelDTO create(LabelDTO labelData) {
        var label = mapper.map(labelData);

        labelRepository.save(label);
        return mapper.map(label);
    }

    public void delete(Long id) {
        labelRepository.deleteById(id);
    }
}
