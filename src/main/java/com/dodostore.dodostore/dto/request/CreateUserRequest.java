package com.dodostore.dodostore.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateUserRequest {

    @jakarta.validation.constraints.NotBlank(message = "USERNAME_INVALID", groups = Registration.class)
    @Size(min = 3, message = "USERNAME_INVALID")
    String username;

    @jakarta.validation.constraints.NotBlank(message = "PASSWORD_INVALID", groups = Registration.class)
    @Size(min = 8, message = "PASSWORD_INVALID")
    String password;

    @jakarta.validation.constraints.NotBlank(message = "Vui lòng nhập họ và tên.", groups = Registration.class)
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự.", groups = Registration.class)
    String fullName;
    String gender;
    String phone;
    @jakarta.validation.constraints.NotBlank(message = "Vui lòng nhập địa chỉ email.", groups = Registration.class)
    @jakarta.validation.constraints.Email(message = "Địa chỉ email không hợp lệ.", groups = Registration.class)
    @Size(max = 254, message = "Email tối đa 254 ký tự.", groups = Registration.class)
    String email;
    String avatar;
}
