package br.com.vcoroa.ecommerce.platform.application.mapper;

import br.com.vcoroa.ecommerce.platform.application.dto.response.OrderDTO;
import br.com.vcoroa.ecommerce.platform.application.dto.response.OrderItemDTO;
import br.com.vcoroa.ecommerce.platform.domain.entity.Order;
import br.com.vcoroa.ecommerce.platform.domain.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "uuid", target = "id")
    @Mapping(source = "user.uuid", target = "userId")
    OrderDTO toDTO(Order order);

    @Mapping(source = "product.uuid", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    OrderItemDTO toItemDTO(OrderItem item);
}
