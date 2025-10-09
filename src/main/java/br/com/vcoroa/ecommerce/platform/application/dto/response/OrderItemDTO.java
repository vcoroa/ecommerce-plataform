package br.com.vcoroa.ecommerce.platform.application.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class OrderItemDTO {
    private UUID productId;
    private String productName;
    private Integer quantity;
    private BigDecimal priceAtPurchase;
}
