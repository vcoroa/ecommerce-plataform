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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductElasticsearchRepository elasticsearchRepository;
    private final ProductMapper productMapper;

    public ProductDTO createProduct(CreateProductRequest request) {
        Product product = productMapper.toEntity(request);
        Product saved = productRepository.save(product);

        ProductDocument doc = productMapper.toDocument(saved);
        elasticsearchRepository.save(doc);

        return productMapper.toDTO(saved);
    }

    public ProductDTO updateProduct(UUID uuid, UpdateProductRequest request) {
        Product product = productRepository.findByUuid(uuid)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        productMapper.updateEntity(request, product);
        Product updated = productRepository.save(product);

        ProductDocument doc = productMapper.toDocument(updated);
        elasticsearchRepository.save(doc);

        return productMapper.toDTO(updated);
    }

    public void deleteProduct(UUID uuid) {
        Product product = productRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        productRepository.delete(product);

        elasticsearchRepository.deleteById(uuid.toString());
    }

    public ProductDTO getProduct(UUID uuid) {
        Product product = productRepository.findByUuid(uuid)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return productMapper.toDTO(product);
    }
}
