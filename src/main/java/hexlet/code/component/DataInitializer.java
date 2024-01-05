package hexlet.code.component;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.Label;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        Optional<User> existingUser = userRepository.findByEmail(admin.get("email"));
        if (existingUser.isEmpty()) {
            var userData = new UserCreateDTO();
            userData.setEmail(admin.get("email"));
            userData.setPassword(admin.get("password"));
            var user = userMapper.map(userData);
            userRepository.save(user);
        }

        var taskStatusNames = taskStatuses.keySet();
        for (String name : taskStatusNames) {
            if (taskStatusRepository.findByName(name).isEmpty()) {
                var slug = taskStatuses.get(name);
                var status = new TaskStatus();
                status.setName(name);
                status.setSlug(slug);
                taskStatusRepository.save(status);
            }
        }

        for (String labelname : labels) {
            if (labelRepository.findByName(labelname).isEmpty()) {
                var label = new Label();
                label.setName(labelname);
                labelRepository.save(label);
            }
        }

    }
}
