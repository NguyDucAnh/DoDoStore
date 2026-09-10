package com.dodostore.dodostore.mapper;

import com.dodostore.dodostore.dto.response.ImageResponse;
import com.dodostore.dodostore.entity.ImageEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    ImageResponse toImageResponse(ImageEntity imageEntity);
}
