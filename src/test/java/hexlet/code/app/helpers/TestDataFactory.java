package hexlet.code.app.helpers;

import hexlet.code.app.model.Label;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import lombok.Getter;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@Getter
public class TestDataFactory {

    @Autowired
    private Faker faker;

    @Autowired
    private PasswordEncoder encoder;

    public User makeUser() {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getPassword), () -> encoder.encode(faker.internet().password()))
                .supply(Select.field(User::getTasks), () -> new ArrayList<>())
                .create();
    }

    public TaskStatus makeTaskStatus() {
        return Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .supply(Select.field(TaskStatus::getName), () -> faker.name().title())
                .supply(Select.field(TaskStatus::getSlug), () -> faker.name().title())
                .ignore(Select.field(TaskStatus::getTasks))
                .create();
    }

    public Label makeLabel() {
        return Instancio.of(Label.class)
                .ignore(Select.field(Label::getId))
                .supply(Select.field(Label::getName), () -> faker.name().title())
                .ignore(Select.field(Label::getTasks))
                .create();
    }

    public Task makeTask() {
        return Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .supply(Select.field(Task::getName), () -> faker.name().firstName())
                .supply(Select.field(Task::getIndex), () -> faker.number().randomDigit())
                .supply(Select.field(Task::getDescription), () -> faker.text().text(30))
                .ignore(Select.field(Task::getTaskStatus))
                .ignore(Select.field(Task::getLabels))
                .ignore(Select.field(Task::getAssignee))
                .create();
    }
}
