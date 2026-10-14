package com.dodostore.dodostore.repository;

import com.dodostore.dodostore.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, Long> {
    List<CartEntity> findByUserId(Long userId);
    CartEntity findByUserIdAndProductIdAndSizeAndColor(Long userId, Long productId, String size, String color);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select c from CartEntity c where c.user.id = :userId and c.id = :cartId")
    CartEntity findForQuantityUpdate(@org.springframework.data.repository.query.Param("userId") Long userId,
                                    @org.springframework.data.repository.query.Param("cartId") Long cartId);
    void deleteByUserIdAndId(Long userId, Long productId);
    void deleteByUserId(Long userId);
}
