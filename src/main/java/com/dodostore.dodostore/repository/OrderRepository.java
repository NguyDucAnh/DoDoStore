package com.dodostore.dodostore.repository;

import com.dodostore.dodostore.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByUserId(Long userId);

    @Query(value = "SELECT DATE(o.date) AS date, SUM(o.total) AS revenue " +
               "FROM orders o WHERE o.date BETWEEN :startDate AND :endDate " +
               "AND o.status_order NOT IN ('Cho_Thanh_Toan', 'Da_Huy') " +
               "GROUP BY DATE(o.date) " +
               "ORDER BY DATE(o.date) ASC", nativeQuery = true)
    List<Object[]> findRevenueByDay(@Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate);
}
