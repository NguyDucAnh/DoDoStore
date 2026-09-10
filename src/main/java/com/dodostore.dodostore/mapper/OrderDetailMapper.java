package com.dodostore.dodostore.mapper;

import com.dodostore.dodostore.dto.response.OrderDetailResponse;
import com.dodostore.dodostore.entity.OrderDetailEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderDetailMapper {
    OrderDetailResponse toOrderDetailResponse(OrderDetailEntity order);
}
