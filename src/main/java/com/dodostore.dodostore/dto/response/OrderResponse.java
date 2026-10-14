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
    public String getFormattedDate() {
        return date == null ? "" : java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
            .withZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).format(date.toInstant());
    }
    Long id;
    Boolean cartDraft;
    Date date;
    Long total;
    Long quantity;
    String phone;
    String address;
    StatusOrderEnum status;
    UserEntity user;
    List<OrderDetailEntity> items;
}
