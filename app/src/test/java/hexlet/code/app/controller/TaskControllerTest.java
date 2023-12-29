package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.model.Label;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import jakarta.transaction.Transactional;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.HashSet;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class TaskControllerTest {

    private final String baseUrl = "/api/tasks";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskMapper mapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private Faker faker;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private TaskStatus testTaskStatus;
    private Task testTask;
    private Label testLabel;

    @BeforeEach
    public void setUp() {
        testUser = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getPassword), () -> passwordEncoder.encode(faker.internet().password()))
                .create();

        userRepository.save(testUser);

        testTaskStatus = Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .supply(Select.field(TaskStatus::getName), () -> faker.name().title())
                .supply(Select.field(TaskStatus::getSlug), () -> "testSlug")
                .ignore(Select.field(TaskStatus::getTasks))
                .create();

        taskStatusRepository.save(testTaskStatus);

        testLabel = Instancio.of(Label.class)
                .ignore(Select.field(Label::getId))
                .supply(Select.field(Label::getName), () -> faker.name().title())
                .ignore(Select.field(Label::getTasks))
                .create();

        labelRepository.save(testLabel);

        testTask = Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .supply(Select.field(Task::getName), () -> faker.name().firstName())
                .supply(Select.field(Task::getIndex), () -> faker.number().randomDigit())
                .supply(Select.field(Task::getDescription), () -> faker.text().text(30))
                .supply(Select.field(Task::getTaskStatus), () -> testTaskStatus)
                .ignore(Select.field(Task::getLabels))
                .supply(Select.field(Task::getAssignee), () -> testUser)
                .create();
    }

    @Test
    public void testShow() throws Exception {
        taskRepository.save(testTask);
        var request = get(baseUrl + "/" + testTask.getId()).with(jwt());

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("title").isEqualTo(testTask.getName()),
                v -> v.node("assignee_id").isEqualTo(testTask.getAssignee().getId()),
                v -> v.node("content").isEqualTo(testTask.getDescription())
        );
    }

    @Test
    public void testIndex() throws Exception {
        taskRepository.save(testTask);

        var request = get(baseUrl).with(jwt());

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray();
    }

    @Test
    public void testCreate() throws Exception {
        var data = new HashMap<>();
        var labelIdSet = new HashSet<>();
        labelIdSet.add(testLabel.getId());
        data.put("index", 12);
        data.put("assignee_id", testUser.getId());
        data.put("title", "Test title");
        data.put("content", "Some content");
        data.put("status", testTaskStatus.getSlug());
        data.put("taskLabelIds", labelIdSet);

        var request = post(baseUrl).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var task = taskRepository.findByName("Test title").get();

        assertThat(task).isNotNull();
        assertThat(task.getIndex()).isEqualTo(12);
        assertThat(task.getAssignee()).isEqualTo(testUser);
        assertThat(task.getDescription()).isEqualTo("Some content");
        assertThat(task.getTaskStatus()).isEqualTo(testTaskStatus);
        assertThat(task.getLabels()).contains(testLabel);
    }

    @Test
    public void testUpdate() throws Exception {
        testTask.setTaskStatus(testTaskStatus);
        taskRepository.save(testTask);

        var data = new HashMap<>();
        data.put("title", "newTitle");
        data.put("content", "newContent");

        var request = put(baseUrl + "/" + testTask.getId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var task = taskRepository.findById(testTask.getId()).get();
        assertThat(task.getName()).isEqualTo("newTitle");
        assertThat(task.getDescription()).isEqualTo("newContent");
    }

    @Test
    public void testDelete() throws Exception {
        testTask.setTaskStatus(testTaskStatus);
        taskRepository.save(testTask);

        var request = delete(baseUrl + "/" + testTask.getId())
                .with(jwt());

        mockMvc.perform(request)
                .andExpect(status().isOk());

        assertThat(taskRepository.findAll().contains(testTask)).isFalse();
    }

}
