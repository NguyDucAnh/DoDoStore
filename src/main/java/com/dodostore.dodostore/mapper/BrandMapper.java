package com.dodostore.dodostore.mapper;

import com.dodostore.dodostore.dto.request.BrandRequest;
import com.dodostore.dodostore.dto.response.BrandResponse;
import com.dodostore.dodostore.entity.BrandEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    BrandResponse toBrandResponse(BrandEntity brandEntity);

    BrandEntity toEntity(BrandRequest brandRequest);
}
