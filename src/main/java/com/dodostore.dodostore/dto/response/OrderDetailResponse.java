package com.dodostore.dodostore.dto.response;


import com.dodostore.dodostore.entity.OrderEntity;
import com.dodostore.dodostore.entity.ProductEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetailResponse {
    Long id;
    Long quantity;
    Long price;
    Long discount;
    Long total;
    OrderEntity order;
    ProductEntity product;
}
