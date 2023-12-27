package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@AutoConfigureMockMvc
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
    private ObjectMapper om;

    @Autowired
    private Faker faker;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private TaskStatus testTaskStatus;
    private Task testTask;

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
                .supply(Select.field(TaskStatus::getName), () -> faker.name().firstName())
                .supply(Select.field(TaskStatus::getSlug), () -> faker.name().lastName())
                .ignore(Select.field(TaskStatus::getTasks))
                .create();

        taskStatusRepository.save(testTaskStatus);

        testTask = Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .supply(Select.field(Task::getName), () -> faker.name().firstName())
                .supply(Select.field(Task::getIndex), () -> faker.number().randomDigit())
                .supply(Select.field(Task::getDescription), () -> faker.text().text(30))
                .supply(Select.field(Task::getTaskStatus), () -> testTaskStatus)
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
        data.put("index", 12);
        data.put("assignee_id", 1);
        data.put("title", "Test title");
        data.put("content", "Aaaaaaaa");
        data.put("status", "draft");

//        var dto = mapper.map(testTask);

        var request = post(baseUrl).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var task = taskRepository.findByName("Test title").get();

        assertThat(task).isNotNull();
        assertThat(task.getName()).isEqualTo("Test title");
        assertThat(task.getIndex()).isEqualTo(12);
    }

    @Test
    public void testUpdate() throws Exception {
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
        taskRepository.save(testTask);

        var request = delete(baseUrl + "/" + testTask.getId())
                .with(jwt());

        mockMvc.perform(request)
                .andExpect(status().isOk());

        assertThat(taskRepository.findAll().contains(testTask)).isFalse();
    }

}
