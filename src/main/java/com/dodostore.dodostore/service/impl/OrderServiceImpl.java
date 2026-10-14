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

    private List<OrderEntity> newestFirst(List<OrderEntity> orders) {
        return orders.stream().sorted(java.util.Comparator
                .comparing(OrderEntity::getDate, java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder()))
                .thenComparing(OrderEntity::getId, java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public Long getCount(){
        return orderRepository.count();
    }

    @Override
    public Long getIncrease(){
        return orderRepository.findAll()
                       .stream()
                       .filter(order -> !Boolean.TRUE.equals(order.getCartDraft()))
                       .filter(order -> order.getStatus() != StatusOrderEnum.Cho_Thanh_Toan && order.getStatus() != StatusOrderEnum.Da_Huy)
                       .filter(order -> order.getTotal() != null)
                       .mapToLong(order -> order.getTotal())
                       .sum();
    }

    @Override
    public List<OrderResponse> getAllOrder() {
        List<OrderResponse> result = new ArrayList<>();

        for(OrderEntity order : newestFirst(orderRepository.findAll())){
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

        order.setStatus(StatusOrderEnum.Dang_Xu_Ly);
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

        for(OrderEntity order : newestFirst(orderRepository.findByUserId(userId))){
            result.add(orderMapper.toOrderResponse(order));
        }
        return result;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void updateStatusOrder(Long orderId, StatusOrderEnum status){
        if (status == null) throw new IllegalArgumentException("Vui lòng chọn trạng thái.");
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng."));
        entityManager.refresh(order, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);

        if (Boolean.TRUE.equals(order.getCartDraft()))
            throw new IllegalStateException("Đơn đang ở giỏ hàng. Khách cần đặt hàng trước khi chuyển trạng thái.");
        if (order.getStatus() == status) return;
        if (order.getStatus() == StatusOrderEnum.Cho_Thanh_Toan || order.getStatus() == StatusOrderEnum.Da_Huy)
            throw new IllegalStateException("Không thể giao đơn chưa thanh toán hoặc đã hủy.");
        if (status == StatusOrderEnum.Cho_Thanh_Toan || status == StatusOrderEnum.Da_Huy)
            throw new IllegalArgumentException("Trạng thái này chỉ được cập nhật bởi dịch vụ thanh toán.");
        order.setStatus(status);

        orderRepository.save(order);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void confirmReceived(Long userId, Long orderId) {
        if (userId == null) throw new IllegalArgumentException("Vui lòng đăng nhập lại.");
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng."));
        entityManager.refresh(order, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if (order.getUser() == null || !userId.equals(order.getUser().getId()))
            throw new IllegalArgumentException("Bạn không có quyền xác nhận đơn hàng này.");
        if (Boolean.TRUE.equals(order.getCartDraft()))
            throw new IllegalStateException("Đơn hàng chưa được đặt.");
        // Repeated submissions are harmless; inventory and payment are unchanged.
        if (order.getStatus() == StatusOrderEnum.Da_Giao) return;
        if (order.getStatus() != StatusOrderEnum.Dang_Giao)
            throw new IllegalStateException("Chỉ có thể xác nhận nhận hàng cho đơn đang giao.");
        order.setStatus(StatusOrderEnum.Da_Giao);
        orderRepository.save(order);
    }

    @Override
    public List<DailyRevenue> getRevenueByDay(LocalDate startDate, LocalDate endDate) {
        // Chuyển đổi LocalDate sang LocalDateTime
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Object[]> rawData = orderRepository.findRevenueByDay(startDateTime, endDateTime);
        java.util.Map<String, BigDecimal> amounts = new java.util.HashMap<>();
        for (Object[] row : rawData) amounts.put(row[0].toString().substring(0, 10), new BigDecimal(row[1].toString()));
        List<DailyRevenue> dailyRevenues = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1))
            dailyRevenues.add(new DailyRevenue(date.toString(), amounts.getOrDefault(date.toString(), BigDecimal.ZERO)));

        return dailyRevenues;
    }
}
