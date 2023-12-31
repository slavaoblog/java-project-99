package hexlet.code.app.component;

import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.dto.UserDTO;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.model.Label;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final Map<String, String> admin = Map.of(
            "email", "hexlet@example.com",
            "password", "qwerty");

    private final Map<String, String> taskStatuses = Map.of(
            "Draft", "draft",
            "ToReview", "to_review",
            "ToBeFixed", "to_be_fixed",
            "ToPublish", "to_publish",
            "Published", "published");

    private final List<String> labels = List.of("feature", "bug");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var userData = new UserCreateDTO();
        userData.setEmail(admin.get("email"));
        userData.setPassword(admin.get("password"));
        var user = userMapper.map(userData);
        userRepository.save(user);

        var taskStatusNames = taskStatuses.keySet();
        for (String name : taskStatusNames) {
            var slug = taskStatuses.get(name);
            var status = new TaskStatus();
            status.setName(name);
            status.setSlug(slug);
            taskStatusRepository.save(status);
        }

        for (String labelname : labels) {
            var label = new Label();
            label.setName(labelname);
            labelRepository.save(label);
        }

    }
}
