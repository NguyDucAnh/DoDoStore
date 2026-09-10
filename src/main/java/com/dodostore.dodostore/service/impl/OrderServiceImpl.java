package com.dodostore.dodostore.service.impl;

import com.dodostore.dodostore.dto.response.DailyRevenue;
import com.dodostore.dodostore.dto.response.OrderResponse;
import com.dodostore.dodostore.entity.OrderDetailEntity;
import com.dodostore.dodostore.entity.OrderEntity;
import com.dodostore.dodostore.entity.UserEntity;
import com.dodostore.dodostore.enums.StatusOrderEnum;
import com.dodostore.dodostore.mapper.OrderMapper;
import com.dodostore.dodostore.repository.OrderDetailRepository;
import com.dodostore.dodostore.repository.OrderRepository;
import com.dodostore.dodostore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService{
    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public Long getCount(){
        return orderRepository.count();
    }

    @Override
    public Long getIncrease(){
        return orderRepository.findAll()
                       .stream()
                       .filter(order -> order.getStatus() != StatusOrderEnum.Cho_Thanh_Toan && order.getStatus() != StatusOrderEnum.Da_Huy)
                       .filter(order -> order.getTotal() != null)
                       .mapToLong(order -> order.getTotal())
                       .sum();
    }

    @Override
    public List<OrderResponse> getAllOrder() {
        List<OrderResponse> result = new ArrayList<>();

        for(OrderEntity order : orderRepository.findAll()){
            result.add(orderMapper.toOrderResponse(order));
        }
        return result;
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {
        return orderMapper.toOrderResponse(orderRepository.findById(orderId).get());
    }

    @Override
    public Long AddOrder(UserEntity user) {

        OrderEntity order = new OrderEntity();

        order.setDate(new Date());
        order.setUser(user);

        orderRepository.save(order);

        return  order.getId();
    }

    @Override
    public void UpdateToTalPrice(Long orderId) {
        Long totalPrice = 0L;
        List<OrderDetailEntity> orderDetails = orderDetailRepository.findByOrderId(orderId);
        for(OrderDetailEntity item : orderDetails){
            totalPrice += item.getTotal();
        }

        OrderEntity order = orderRepository.findById(orderId).get();

        order.setTotal(totalPrice);
        order.setQuantity(Long.valueOf(orderDetails.size()));

        orderRepository.save(order);
    }

    @Override
    public List<OrderResponse> historyBuy(Long userId) {
        List<OrderResponse> result = new ArrayList<>();

        for(OrderEntity order : orderRepository.findByUserId(userId)){
            result.add(orderMapper.toOrderResponse(order));
        }
        return result;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void updateStatusOrder(Long orderId, StatusOrderEnum status){
        OrderEntity order = orderRepository.findById(orderId).get();
        entityManager.refresh(order, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);

        if (order.getStatus() == StatusOrderEnum.Cho_Thanh_Toan || order.getStatus() == StatusOrderEnum.Da_Huy)
            throw new IllegalStateException("Không thể giao đơn chưa thanh toán hoặc đã hủy.");
        if (status == StatusOrderEnum.Cho_Thanh_Toan || status == StatusOrderEnum.Da_Huy)
            throw new IllegalArgumentException("Trạng thái này chỉ được cập nhật bởi dịch vụ thanh toán.");
        order.setStatus(status);

        orderRepository.save(order);
    }

    @Override
    public List<DailyRevenue> getRevenueByDay(LocalDate startDate, LocalDate endDate) {
        // Chuyển đổi LocalDate sang LocalDateTime
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Object[]> rawData = orderRepository.findRevenueByDay(startDateTime, endDateTime);
        List<DailyRevenue> dailyRevenues = new ArrayList<>();

        for (Object[] row : rawData) {
            String date = ((Date) row[0]).toString();
            BigDecimal revenue = (BigDecimal) row[1];
            dailyRevenues.add(new DailyRevenue(date, revenue));
        }

        return dailyRevenues;
    }
}
