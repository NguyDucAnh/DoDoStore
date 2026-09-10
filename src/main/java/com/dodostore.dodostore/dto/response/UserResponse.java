package com.dodostore.dodostore.dto.response;

import com.dodostore.dodostore.entity.AddressEntity;
import com.dodostore.dodostore.entity.CartEntity;
import com.dodostore.dodostore.enums.StatusEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Long id;
    String username;
    String password;
    String fullName;
    String gender;
    String phone;
    String email;
    String avatar;
    Date createDate;
    Date updateDate;
    String roles;
    StatusEnum status;
    List<CartEntity> carts;
    List<AddressEntity> addresses;
}
