package br.com.vcoroa.ecommerce.platform.application.mapper;

import br.com.vcoroa.ecommerce.platform.application.dto.request.RegisterRequest;
import br.com.vcoroa.ecommerce.platform.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(source = "encodedPassword", target = "password")
    User toEntity(RegisterRequest request, String encodedPassword);
}
