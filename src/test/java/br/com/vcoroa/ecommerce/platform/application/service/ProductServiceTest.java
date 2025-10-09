package br.com.vcoroa.ecommerce.platform.application.service;

import br.com.vcoroa.ecommerce.platform.application.dto.request.CreateProductRequest;
import br.com.vcoroa.ecommerce.platform.application.dto.request.UpdateProductRequest;
import br.com.vcoroa.ecommerce.platform.application.dto.response.ProductDTO;
import br.com.vcoroa.ecommerce.platform.application.mapper.ProductMapper;
import br.com.vcoroa.ecommerce.platform.domain.entity.Product;
import br.com.vcoroa.ecommerce.platform.domain.entity.ProductDocument;
import br.com.vcoroa.ecommerce.platform.domain.exception.ResourceNotFoundException;
import br.com.vcoroa.ecommerce.platform.infrastructure.repository.ProductElasticsearchRepository;
import br.com.vcoroa.ecommerce.platform.infrastructure.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductElasticsearchRepository elasticsearchRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private CreateProductRequest createRequest;
    private ProductDTO expectedDto;
    private Product savedProduct;
    private ProductDocument productDocument;

    @BeforeEach
    void setUp() {
        createRequest = new CreateProductRequest();
        createRequest.setName("MacBook Pro");
        createRequest.setDescription("Apple laptop");
        createRequest.setPrice(BigDecimal.valueOf(1999.99));
        createRequest.setCategory("ELECTRONICS");
        createRequest.setStockQuantity(5);

        UUID productId = UUID.randomUUID();
        savedProduct = Product.builder()
                .id(1)
                .uuid(productId)
                .name(createRequest.getName())
                .description(createRequest.getDescription())
                .price(createRequest.getPrice())
                .category(createRequest.getCategory())
                .stockQuantity(createRequest.getStockQuantity())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        expectedDto = new ProductDTO();
        expectedDto.setId(productId);
        expectedDto.setName(createRequest.getName());
        expectedDto.setDescription(createRequest.getDescription());
        expectedDto.setPrice(createRequest.getPrice());
        expectedDto.setCategory(createRequest.getCategory());
        expectedDto.setStockQuantity(createRequest.getStockQuantity());
        expectedDto.setCreatedAt(savedProduct.getCreatedAt());
        expectedDto.setUpdatedAt(savedProduct.getUpdatedAt());

        productDocument = new ProductDocument(
                productId.toString(),
                savedProduct.getName(),
                savedProduct.getDescription(),
                savedProduct.getPrice(),
                savedProduct.getCategory(),
                savedProduct.getStockQuantity(),
                savedProduct.getCreatedAt().toString(),
                savedProduct.getUpdatedAt().toString()
        );
    }

    @Test
    void createProduct_shouldPersistAndIndexProduct() {
        Product transientProduct = new Product();

        when(productMapper.toEntity(createRequest)).thenReturn(transientProduct);
        when(productRepository.save(transientProduct)).thenReturn(savedProduct);
        when(productMapper.toDocument(savedProduct)).thenReturn(productDocument);
        when(productMapper.toDTO(savedProduct)).thenReturn(expectedDto);

        ProductDTO result = productService.createProduct(createRequest);

        assertThat(result).isEqualTo(expectedDto);
        verify(productRepository).save(transientProduct);
        verify(productMapper).toDocument(savedProduct);
        verify(elasticsearchRepository).save(productDocument);
        verify(productMapper).toDTO(savedProduct);
        verifyNoMoreInteractions(productRepository, productMapper, elasticsearchRepository);
    }

    @Test
    void updateProduct_shouldUpdateExistingProduct() {
        UUID productId = savedProduct.getUuid();
        UpdateProductRequest updateRequest = new UpdateProductRequest();
        updateRequest.setName("Updated");
        updateRequest.setDescription("Updated description");
        updateRequest.setPrice(BigDecimal.valueOf(2999.99));
        updateRequest.setCategory("LAPTOPS");
        updateRequest.setStockQuantity(10);

        when(productRepository.findByUuid(productId)).thenReturn(Optional.of(savedProduct));
        when(productRepository.save(savedProduct)).thenReturn(savedProduct);
        when(productMapper.toDocument(savedProduct)).thenReturn(productDocument);
        when(productMapper.toDTO(savedProduct)).thenReturn(expectedDto);

        ProductDTO result = productService.updateProduct(productId, updateRequest);

        assertThat(result).isEqualTo(expectedDto);
        verify(productMapper).updateEntity(updateRequest, savedProduct);
        verify(productRepository).save(savedProduct);
        verify(elasticsearchRepository).save(productDocument);
    }

    @Test
    void updateProduct_shouldThrowWhenProductNotFound() {
        UUID productId = UUID.randomUUID();
        UpdateProductRequest updateRequest = new UpdateProductRequest();

        when(productRepository.findByUuid(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(productId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void deleteProduct_shouldRemoveFromDatabaseAndIndex() {
        UUID productId = savedProduct.getUuid();
        when(productRepository.findByUuid(productId)).thenReturn(Optional.of(savedProduct));

        productService.deleteProduct(productId);

        verify(productRepository).delete(savedProduct);
        verify(elasticsearchRepository).deleteById(productId.toString());
    }

    @Test
    void getProduct_shouldReturnMappedDto() {
        UUID productId = savedProduct.getUuid();
        when(productRepository.findByUuid(productId)).thenReturn(Optional.of(savedProduct));
        when(productMapper.toDTO(savedProduct)).thenReturn(expectedDto);

        ProductDTO result = productService.getProduct(productId);

        assertThat(result).isEqualTo(expectedDto);
        verify(productMapper).toDTO(savedProduct);
    }

    @Test
    void getProduct_shouldThrowWhenNotFound() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findByUuid(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(productId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }
}
