package br.com.vcoroa.ecommerce.platform.presentation.controller;

import br.com.vcoroa.ecommerce.platform.domain.entity.User;
import br.com.vcoroa.ecommerce.platform.domain.enums.Role;
import br.com.vcoroa.ecommerce.platform.infrastructure.messaging.event.OrderPaidEvent;
import br.com.vcoroa.ecommerce.platform.infrastructure.repository.ProductElasticsearchRepository;
import br.com.vcoroa.ecommerce.platform.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private ProductElasticsearchRepository productElasticsearchRepository;

    @MockBean
    private ElasticsearchOperations elasticsearchOperations;

    @MockBean(name = "elasticsearchMappingContext")
    private SimpleElasticsearchMappingContext elasticsearchMappingContext;

    @MockBean
    private KafkaTemplate<String, OrderPaidEvent> kafkaTemplate;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void register_shouldCreateUser() throws Exception {
        String payload = "{" +
                "\"username\":\"john\"," +
                "\"email\":\"john@example.com\"," +
                "\"password\":\"StrongPass123!\"," +
                "\"role\":\"USER\"" +
                "}";

        mockMvc.perform(post("/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User registered successfully")));

        User saved = userRepository.findByUsername("john").orElseThrow();
        assertThat(passwordEncoder.matches("StrongPass123!", saved.getPassword())).isTrue();
        assertThat(saved.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void login_shouldReturnJwtToken() throws Exception {
        User user = User.builder()
                .username("mary")
                .email("mary@example.com")
                .password(passwordEncoder.encode("Pass123!"))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        String payload = "{" +
                "\"username\":\"mary\"," +
                "\"password\":\"Pass123!\"" +
                "}";

        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.username").value("mary"))
                .andExpect(jsonPath("$.role").value("USER"));
    }
}
