package hexlet.code.service;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return mapper.map(user);
    }

    public UserDTO update(UserUpdateDTO userUpdateDTO, Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id" + id + " not found"));
        mapper.update(userUpdateDTO, user);
        userRepository.save(user);
        return mapper.map(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
