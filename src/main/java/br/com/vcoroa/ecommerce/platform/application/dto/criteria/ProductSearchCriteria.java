package br.com.vcoroa.ecommerce.platform.application.dto.criteria;

import lombok.Data;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

@Data
public class ProductSearchCriteria {
    private String name;
    private String category;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Pageable pageable;
}
