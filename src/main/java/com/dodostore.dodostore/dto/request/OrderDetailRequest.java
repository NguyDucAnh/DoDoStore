package com.dodostore.dodostore.dto.request;

import com.dodostore.dodostore.entity.OrderEntity;
import com.dodostore.dodostore.entity.ProductEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetailRequest {
    Long id;
    Long quantity;
    OrderEntity order;
    ProductEntity product;
}
