package com.dodostore.dodostore.mapper;

import com.dodostore.dodostore.dto.request.CategoryRequest;
import com.dodostore.dodostore.dto.response.CategoryResponse;
import com.dodostore.dodostore.entity.CategoryEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toCategoryResponse(CategoryEntity category);

    CategoryEntity toEntity(CategoryRequest categoryRequest);
}
