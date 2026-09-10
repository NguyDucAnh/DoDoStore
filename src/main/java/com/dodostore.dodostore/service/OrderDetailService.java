package com.dodostore.dodostore.service;

import com.dodostore.dodostore.dto.response.OrderDetailResponse;
import com.dodostore.dodostore.entity.CartEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OrderDetailService {
    // Add Item
    void AddItem(List<CartEntity> carts, Long orderId);

    // Get Item By Order Id
    List<OrderDetailResponse> getItemByOrderId(Long orderId);
} 
