package com.dodostore.dodostore.mapper;

import com.dodostore.dodostore.dto.response.OrderResponse;
import com.dodostore.dodostore.entity.OrderEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderResponse toOrderResponse(OrderEntity order);
}