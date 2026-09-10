package com.dodostore.dodostore.mapper;

import com.dodostore.dodostore.dto.request.ProductRequest;
import com.dodostore.dodostore.dto.response.ProductResponse;
import com.dodostore.dodostore.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductResponse toProductResponse(ProductEntity productEntity);
    ProductEntity toProductEntity(ProductRequest productRequest);
    void updateProductEntity(@MappingTarget ProductEntity productEntity, ProductRequest productRequest);
}
