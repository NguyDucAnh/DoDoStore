package com.dodostore.dodostore.repository;

import com.dodostore.dodostore.entity.ProductEntity;
import com.dodostore.dodostore.enums.StatusEnum;
import com.dodostore.dodostore.repository.customer.ProductRepositoryCustomer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long>, ProductRepositoryCustomer {
    boolean existsByNameAndStatus(String name, StatusEnum status);

    List<ProductEntity> findByStatus(StatusEnum status);

    List<ProductEntity> findByCategoryId(Long categoryId);

    List<ProductEntity> findByBrandId(Long brandId);

    boolean existsByIdAndStatus(Long id, StatusEnum status);

    ProductEntity findByNameAndStatus(String name, StatusEnum status);

    ProductEntity findByIdAndStatus(Long productId, StatusEnum status);

    List<ProductEntity> findByNameContainingOrDescriptionContainingAndStatus(String name, String description, StatusEnum status);

    // Thêm 2 methods này vào ProductRepository
    Page<ProductEntity> findByStatus(StatusEnum status, Pageable pageable);
    Page<ProductEntity> findByNameContainingIgnoreCaseAndStatus(String name, StatusEnum status, Pageable pageable);
}
