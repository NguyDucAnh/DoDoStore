package com.dodostore.dodostore.service;

import com.dodostore.dodostore.dto.response.DailyRevenue;
import com.dodostore.dodostore.dto.response.OrderResponse;
import com.dodostore.dodostore.entity.UserEntity;
import com.dodostore.dodostore.enums.StatusOrderEnum;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public interface OrderService {
    // View count Order
    Long getCount();

    Long getIncrease();

    // View all Order
    List<OrderResponse> getAllOrder();

    // Get Order By orderId;
    OrderResponse getOrderById(Long orderId);

    // AddOrder
    Long AddOrder(UserEntity user);

    // Update ToTal Price
    void UpdateToTalPrice(Long orderId);

    // Get Order By UserId
    List<OrderResponse> historyBuy(Long userId);

    void confirmReceived(Long userId, Long orderId);

    // Update Status Order
    void updateStatusOrder(Long orderId, StatusOrderEnum status);

    List<DailyRevenue> getRevenueByDay(LocalDate startDate, LocalDate endDate);

}