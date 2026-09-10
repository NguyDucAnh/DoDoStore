package com.dodostore.dodostore.dto.response;

import com.dodostore.dodostore.entity.OrderDetailEntity;
import com.dodostore.dodostore.entity.UserEntity;
import com.dodostore.dodostore.enums.StatusOrderEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    Long id;
    Date date;
    Long total;
    Long quantity;
    String phone;
    String address;
    StatusOrderEnum status;
    UserEntity user;
    List<OrderDetailEntity> items;
}
