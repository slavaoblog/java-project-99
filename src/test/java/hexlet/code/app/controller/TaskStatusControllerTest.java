package hexlet.code.app.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.helpers.TestDataFactory;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.TaskStatusRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TaskStatusControllerTest {

    private final String baseUrl = "/api/task_statuses";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TestDataFactory testDataFactory;

    private TaskStatus testTaskStatus;

    @BeforeEach
    public void setUp() {
        testTaskStatus = testDataFactory.makeTaskStatus();
        taskStatusRepository.save(testTaskStatus);
    }

    @Test
    public void testShow() throws Exception {
        var request = get(baseUrl + "/" + testTaskStatus.getId()).with(jwt());

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(testTaskStatus.getName()),
                v -> v.node("slug").isEqualTo(testTaskStatus.getSlug())
        );
    }

    @Test
    public void testCreate() throws Exception {
        var data = testDataFactory.makeTaskStatus();

        var request = post(baseUrl)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var status = taskStatusRepository.findBySlug(data.getSlug()).get();

        assertThat(status).isNotNull();
        assertThat(status.getName()).isEqualTo(data.getName());
    }

    @Test
    public void testIndex() throws Exception {
        var request = get(baseUrl).with(jwt());

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray();
    }

    @Test
    public void testUpdate() throws Exception {
        var data = new HashMap<>();
        data.put("name", "newName");
        data.put("slug", "newSlug");

        var request = put(baseUrl + "/" + testTaskStatus.getId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var status = taskStatusRepository.findById(testTaskStatus.getId()).get();
        assertThat(status.getName()).isEqualTo("newName");
        assertThat(status.getSlug()).isEqualTo("newSlug");
    }

    @Test
    public void testDelete() throws Exception {
        var slug = testTaskStatus.getSlug();

        var request = delete(baseUrl + "/" + testTaskStatus.getId())
                .with(jwt());

        mockMvc.perform(request)
                .andExpect(status().isOk());

        assertThat(taskStatusRepository.findAll().contains(testTaskStatus)).isFalse();
    }
}


