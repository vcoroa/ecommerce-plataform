package br.com.vcoroa.ecommerce.platform.application.mapper;

import br.com.vcoroa.ecommerce.platform.application.dto.request.CreateProductRequest;
import br.com.vcoroa.ecommerce.platform.application.dto.request.UpdateProductRequest;
import br.com.vcoroa.ecommerce.platform.application.dto.response.ProductDTO;
import br.com.vcoroa.ecommerce.platform.domain.entity.Product;
import br.com.vcoroa.ecommerce.platform.domain.entity.ProductDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(CreateProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateProductRequest request, @MappingTarget Product product);

    @Mapping(source = "uuid", target = "id")
    ProductDTO toDTO(Product product);

    @Mapping(target = "id", expression = "java(product.getUuid().toString())")
    @Mapping(target = "createdAt", expression = "java(formatDateTime(product.getCreatedAt()))")
    @Mapping(target = "updatedAt", expression = "java(formatDateTime(product.getUpdatedAt()))")
    ProductDocument toDocument(Product product);

    default String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FORMATTER) : null;
    }
}
