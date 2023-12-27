package hexlet.code.app.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.TaskStatusRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskStatusControllerTest {

    private final String baseUrl = "/api/task_statuses";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private Faker faker;

    public TaskStatus createTaskStatusForTest() {
        return Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .supply(Select.field(TaskStatus::getName), () -> faker.name().firstName())
                .supply(Select.field(TaskStatus::getSlug), () -> faker.name().lastName())
                .create();
    }

    @Test
    public void testShow() throws Exception {
        var status = createTaskStatusForTest();
        taskStatusRepository.save(status);
        var request = get(baseUrl + "/" + status.getId()).with(jwt());

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(status.getName()),
                v -> v.node("slug").isEqualTo(status.getSlug())
        );
    }

    @Test
    public void testCreate() throws Exception {
        var data = createTaskStatusForTest();

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
        var status = createTaskStatusForTest();
        taskStatusRepository.save(status);

        var request = get(baseUrl).with(jwt());

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray();
    }

    @Test
    public void testUpdate() throws Exception {
        var status = createTaskStatusForTest();
        taskStatusRepository.save(status);

        var data = new HashMap<>();
        data.put("name", "newName");
        data.put("slug", "newSlug");

        var request = put(baseUrl + "/" + status.getId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        status = taskStatusRepository.findById(status.getId()).get();
        assertThat(status.getName()).isEqualTo("newName");
        assertThat(status.getSlug()).isEqualTo("newSlug");
    }

    @Test
    public void testDelete() throws Exception {
        var status = createTaskStatusForTest();
        var slug = status.getSlug();
        taskStatusRepository.save(status);

        var request = delete(baseUrl + "/" + status.getId())
                .with(jwt());

        mockMvc.perform(request)
                .andExpect(status().isOk());

        assertThat(taskStatusRepository.findAll().contains(status)).isFalse();
    }
}


