package com.dodostore.dodostore.repository;

import com.dodostore.dodostore.entity.OrderDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetailEntity, Long> {
    List<OrderDetailEntity> findByOrderId(Long orderId);

    List<OrderDetailEntity> findByProductId(Long productId);
}
