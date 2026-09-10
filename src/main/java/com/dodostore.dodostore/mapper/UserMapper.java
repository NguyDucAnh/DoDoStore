package com.dodostore.dodostore.mapper;


import com.dodostore.dodostore.dto.request.CreateUserRequest;
import com.dodostore.dodostore.dto.request.UpdateUserRequest;
import com.dodostore.dodostore.dto.response.UserResponse;
import com.dodostore.dodostore.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(UserEntity userEntity);

    UserEntity toUserEntity(CreateUserRequest newUser);

    void updateUserEntity(@MappingTarget UserEntity userEntity, UpdateUserRequest updateUserRequest);
}
