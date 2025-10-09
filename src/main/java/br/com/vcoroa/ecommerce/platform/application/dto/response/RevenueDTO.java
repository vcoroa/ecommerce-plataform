package br.com.vcoroa.ecommerce.platform.application.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RevenueDTO {
    private String month;
    private Integer year;
    private BigDecimal totalRevenue;
}
