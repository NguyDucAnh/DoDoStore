package com.dodostore.dodostore.repository;

import com.dodostore.dodostore.entity.CategoryEntity;
import com.dodostore.dodostore.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    boolean existsByNameAndStatus(String name, StatusEnum status);

    CategoryEntity findByIdAndStatus(Long categoryId, StatusEnum status);

    Optional<CategoryEntity> findByName(String name);

    // Thêm 2 methods này vào ProductRepository
    Page<CategoryEntity> findByStatus(StatusEnum status, Pageable pageable);
    Page<CategoryEntity> findByNameContainingIgnoreCaseAndStatus(String name, StatusEnum status, Pageable pageable);

}
