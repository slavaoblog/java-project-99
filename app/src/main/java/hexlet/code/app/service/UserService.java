package hexlet.code.app.service;

import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.dto.UserDTO;
import hexlet.code.app.dto.UserUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper mapper;

    public UserDTO findById(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id" + id + " not found"));

        return mapper.map(user);
    }

    public List<UserDTO> getAll() {
        var users = userRepository.findAll();

        return users.stream()
                .map(mapper::map)
                .toList();
    }

    public UserDTO create(UserCreateDTO userData) {
        var user = mapper.map(userData);
        userRepository.save(user);
        var userDTO = mapper.map(user);
        return userDTO;
    }

    public UserDTO update(UserUpdateDTO userUpdateDTO, Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id" + id + " not found"));
        mapper.update(userUpdateDTO, user);
        userRepository.save(user);
        var userDTO = mapper.map(user);
        return userDTO;
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
