package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.helpers.TestDataFactory;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
public class TaskControllerTest {

    private final String baseUrl = "/api/tasks";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TestDataFactory testDataFactory;

    private User testUser;
    private TaskStatus testTaskStatus;
    private Task testTask;
    private Label testLabel;

    @BeforeEach
    public void setUp() {
        testUser = testDataFactory.makeUser();
        userRepository.save(testUser);

        testTaskStatus = testDataFactory.makeTaskStatus();
        taskStatusRepository.save(testTaskStatus);

        testLabel = testDataFactory.makeLabel();
        labelRepository.save(testLabel);

        testTask = testDataFactory.makeTask();
        testTask.setTaskStatus(testTaskStatus);
        testTask.setAssignee(testUser);
        testTask.setLabels(new HashSet<>(Set.of(testLabel)));
    }

    @Test
    public void testShow() throws Exception {
        var testLabel2 = testDataFactory.makeLabel();
        var testLabel3 = testDataFactory.makeLabel();
        labelRepository.save(testLabel2);
        labelRepository.save(testLabel3);

        testTask.getLabels().add(testLabel2);
        testTask.getLabels().add(testLabel3);

        taskRepository.save(testTask);

        var request = get(baseUrl + "/" + testTask.getId()).with(jwt());

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        var labelSet = testTask.getLabels().stream()
                .map(Label::getId)
                .collect(Collectors.toSet());

        assertThatJson(body).and(
                v -> v.node("title").isEqualTo(testTask.getName()),
                v -> v.node("assignee_id").isEqualTo(testTask.getAssignee().getId()),
                v -> v.node("content").isEqualTo(testTask.getDescription()),
                v -> v.node("taskLabelIds").isEqualTo(labelSet.toString())
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
    public void testIndexWithFilter() throws Exception {
        taskRepository.save(testTask);

        var testTask2 = testDataFactory.makeTask();
        var testUser2 = testDataFactory.makeUser();
        userRepository.save(testUser2);
        var testTaskStatus2 = testDataFactory.makeTaskStatus();
        testTaskStatus2.setSlug("test_slug");
        taskStatusRepository.save(testTaskStatus2);
        var testLabel2 = testDataFactory.makeLabel();
        labelRepository.save(testLabel2);
        testTask2.setName("Test title");
        testTask2.setAssignee(testUser2);
        testTask2.setTaskStatus(testTaskStatus2);
        testTask2.setLabels(new HashSet<>(Set.of(testLabel2)));

        taskRepository.save(testTask2);

        var request = get(
                baseUrl + "?titleCont=tes&status=test_slug&labelId="
                + testLabel2.getId() + "&assigneeId=" + testUser2.getId()
        ).with(jwt());

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray();
        assertThat(body).contains("Test title");
        assertThat(body).doesNotContain(testTask.getName());
        assertThat(body).contains("assignee_id\":" + testTask2.getAssignee().getId());
        assertThat(body).doesNotContain("assignee_id\":" + testTask.getAssignee().getId());
        assertThat(body).contains("test_slug");
        assertThat(body).doesNotContain(testTask.getTaskStatus().getSlug());
        assertThat(body).contains("taskLabelIds\":[" + testLabel2.getId());
        assertThat(body).doesNotContain("taskLabelIds\":[" + testTask.getLabels().toString());
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
                .andExpect(status().isNoContent());

        assertThat(taskRepository.findAll().contains(testTask)).isFalse();
    }

}
