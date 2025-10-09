package br.com.vcoroa.ecommerce.platform.presentation.controller;

import br.com.vcoroa.ecommerce.platform.application.dto.criteria.ProductSearchCriteria;
import br.com.vcoroa.ecommerce.platform.application.dto.request.CreateProductRequest;
import br.com.vcoroa.ecommerce.platform.application.dto.request.UpdateProductRequest;
import br.com.vcoroa.ecommerce.platform.application.dto.response.ProductDTO;
import br.com.vcoroa.ecommerce.platform.application.service.ProductSearchService;
import br.com.vcoroa.ecommerce.platform.application.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductSearchService searchService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductDTO> create(@Valid @RequestBody CreateProductRequest request) {
        ProductDTO product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductDTO product = productService.updateProduct(id, request);
        return ResponseEntity.ok(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getById(@PathVariable UUID id) {
        ProductDTO product = productService.getProduct(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable) {

        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setName(name);
        criteria.setCategory(category);
        criteria.setMinPrice(minPrice);
        criteria.setMaxPrice(maxPrice);
        criteria.setPageable(pageable);

        Page<ProductDTO> results = searchService.search(criteria);
        return ResponseEntity.ok(results);
    }
}
