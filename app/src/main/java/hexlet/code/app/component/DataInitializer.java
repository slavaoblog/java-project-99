package hexlet.code.app.component;

import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final TaskStatusRepository taskStatusRepository;

    @Autowired
    private final UserMapper userMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var userData = new UserCreateDTO();
        userData.setEmail("hexlet@example.com");
        userData.setPassword("qwerty");
        userData.setFirstName("John");
        userData.setLastName("Bundy");
        var user = userMapper.map(userData);
        userRepository.save(user);

        List<TaskStatus> taskStatusList = Arrays.asList(
                new TaskStatus("Draft", "draft"),
                new TaskStatus("To Review", "to_review"),
                new TaskStatus("To Be Fixed", "to_be_fixed"),
                new TaskStatus("To Publish", "to_publish"),
                new TaskStatus("Published", "published")
        );

        taskStatusRepository.saveAll(taskStatusList);

    }
}
