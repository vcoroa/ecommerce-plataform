package br.com.vcoroa.ecommerce.platform.application.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AvgTicketDTO {
    private UUID userId;
    private String username;
    private BigDecimal avgTicket;
    private Integer orderCount;
}
