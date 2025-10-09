package br.com.vcoroa.ecommerce.platform.application.mapper;

import br.com.vcoroa.ecommerce.platform.application.dto.response.AvgTicketDTO;
import br.com.vcoroa.ecommerce.platform.application.dto.response.TopUserDTO;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    default TopUserDTO toTopUserDTO(Object[] row) {
        TopUserDTO dto = new TopUserDTO();
        dto.setUserId((UUID) row[0]);
        dto.setUsername((String) row[1]);
        dto.setTotalPurchase(BigDecimal.valueOf((Double) row[2]));
        dto.setOrderCount(((Long) row[3]).intValue());
        return dto;
    }

    default AvgTicketDTO toAvgTicketDTO(Object[] row) {
        AvgTicketDTO dto = new AvgTicketDTO();
        dto.setUserId((UUID) row[0]);
        dto.setUsername((String) row[1]);
        dto.setAvgTicket(BigDecimal.valueOf((Double) row[2]));
        dto.setOrderCount(((Long) row[3]).intValue());
        return dto;
    }
}
