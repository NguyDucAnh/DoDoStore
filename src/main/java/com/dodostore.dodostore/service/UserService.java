package com.dodostore.dodostore.service;

import com.dodostore.dodostore.dto.request.AddressRequest;
import com.dodostore.dodostore.dto.request.CreateUserRequest;
import com.dodostore.dodostore.dto.request.UpdateUserRequest;
import com.dodostore.dodostore.dto.response.UserResponse;
import com.dodostore.dodostore.entity.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface UserService {

    // View user quantity
    public int countUsers();

    // View all users
    public List<UserResponse> getUsers();

    // View user by Id
    public UserResponse getUserById(Long userId);

    // Register user
    UserEntity registerUser(CreateUserRequest request) ;

    // Create user
    public UserEntity createUser(CreateUserRequest request, MultipartFile file);

    // Update user
    public UserResponse updateUser(UpdateUserRequest request, Long userId, MultipartFile file);

    // Delete user
    public void deleteUser(Long userId);

    // Find By Username
    public UserEntity getUserByUsername(String username);

    // Add Address
    public void addAddress(Long userId, AddressRequest request);

    // Add Address
    public void deleteAddress(Long addressId);

    // Checkout 
    public String checkout(Long userId, Long addressId);
}
