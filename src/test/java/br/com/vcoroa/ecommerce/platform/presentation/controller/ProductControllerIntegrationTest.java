package br.com.vcoroa.ecommerce.platform.presentation.controller;

import br.com.vcoroa.ecommerce.platform.domain.entity.Product;
import br.com.vcoroa.ecommerce.platform.infrastructure.messaging.event.OrderPaidEvent;
import br.com.vcoroa.ecommerce.platform.infrastructure.repository.ProductElasticsearchRepository;
import br.com.vcoroa.ecommerce.platform.infrastructure.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @MockBean
    private ProductElasticsearchRepository productElasticsearchRepository;

    @MockBean
    private ElasticsearchOperations elasticsearchOperations;

    @MockBean(name = "elasticsearchMappingContext")
    private SimpleElasticsearchMappingContext elasticsearchMappingContext;

    @MockBean
    private KafkaTemplate<String, OrderPaidEvent> kafkaTemplate;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createProduct_shouldPersistAndReturnDto() throws Exception {
        String payload = "{" +
                "\"name\":\"MacBook Pro\"," +
                "\"description\":\"Apple laptop\"," +
                "\"price\":1999.99," +
                "\"category\":\"ELECTRONICS\"," +
                "\"stockQuantity\":10" +
                "}";

        mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("MacBook Pro"))
                .andExpect(jsonPath("$.category").value("ELECTRONICS"))
                .andExpect(jsonPath("$.stockQuantity").value(10));

        assertThat(productRepository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "john", roles = {"USER"})
    void getProduct_shouldReturnPersistedProduct() throws Exception {
        Product product = Product.builder()
                .name("iPhone 15")
                .description("Apple smartphone")
                .price(BigDecimal.valueOf(1299.99))
                .category("ELECTRONICS")
                .stockQuantity(50)
                .build();
        Product saved = productRepository.save(product);

        mockMvc.perform(get("/products/" + saved.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("iPhone 15"))
                .andExpect(jsonPath("$.category").value("ELECTRONICS"))
                .andExpect(jsonPath("$.stockQuantity").value(50));
    }
}
