package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    private final String baseUrl = "/api/users";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private Faker faker;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    public User createUserForTest() {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getPassword), () -> passwordEncoder.encode(faker.internet().password()))
                .create();
    }

    @Test
    public void testShow() throws Exception {
        var user = createUserForTest();
        userRepository.save(user);

        var request = get(baseUrl + "/" + user.getId()).with(jwt());

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("firstName").isEqualTo(user.getFirstName()),
                v -> v.node("email").isEqualTo(user.getEmail())
        );
    }

    @Test
    public void testCreate() throws Exception {
        var data = createUserForTest();
        var password = "qwerty";
        data.setPassword(password);

        var request = post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var user = userRepository.findByEmail(data.getEmail()).get();

        assertThat(user).isNotNull();
        assertThat(user.getFirstName()).isEqualTo(data.getFirstName());
        assertThat(passwordEncoder.matches(password, user.getPassword())).isTrue();
    }

    @Test
    public void testCreateWithNotValidEmail() throws Exception {
        var data = createUserForTest();
        data.setEmail("qwert");

        var request = post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateWithNotValidPassword() throws Exception {
        var data = createUserForTest();
        data.setPassword("1");

        var request = post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAutoGeneratedFields() {
        User user = createUserForTest();
        userRepository.save(user);

        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
        assertThat(user.getCreatedAt()).isEqualTo(user.getUpdatedAt());
    }

    @Test
    public void testUpdatePositive() throws Exception {
        var user = createUserForTest();
        userRepository.save(user);

        var data = new HashMap<>();
        var oldPassword = user.getPassword();
        var newPassword = "qwerty12345";
        data.put("firstName", "someFirstName");
        data.put("email", "someEmail@ya.com");
        data.put("password", newPassword);

        var request = put(baseUrl + "/" + user.getId())
                .with(jwt())
                .with(user(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        user = userRepository.findById(user.getId()).get();
        assertThat(user.getFirstName()).isEqualTo("someFirstName");
        assertThat(user.getEmail()).isEqualTo("someEmail@ya.com");
        assertThat(passwordEncoder.matches(newPassword, oldPassword)).isFalse();
        assertThat(passwordEncoder.matches(newPassword, user.getPassword())).isTrue();
    }

    @Test
    @WithMockUser(username = "wrong@wrong.ru")
    public void testUpdateNegative() throws Exception {
        var user = createUserForTest();
        userRepository.save(user);

        var data = new HashMap<>();
        data.put("firstName", "someFirstName");
        data.put("email", "someEmail@ya.com");

        var request = put(baseUrl + "/" + user.getId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    public void testIndex() throws Exception {
        var user = createUserForTest();
        userRepository.save(user);

        var request = get(baseUrl).with(jwt());

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).isArray();
    }

    @Test
    @WithMockUser(username = "test@test.com")
    public void testDeleteWrong() throws Exception {

        var request = delete(baseUrl + "/1");

        var result = mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "hexlet@example.com")
    public void testDelete() throws Exception {

        var request = delete(baseUrl + "/1");

        var result = mockMvc.perform(request)
                .andExpect(status().isOk());
    }
}
