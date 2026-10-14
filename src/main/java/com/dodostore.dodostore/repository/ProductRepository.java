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
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from ProductEntity p where p.status = :status or p.flashSaleCampaign.id = :campaignId order by p.id")
    List<ProductEntity> lockCampaignCandidates(@org.springframework.data.repository.query.Param("status") StatusEnum status,
        @org.springframework.data.repository.query.Param("campaignId") Long campaignId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from ProductEntity p where p.flashSaleCampaign.id = :id order by p.id")
    List<ProductEntity> lockByCampaign(@org.springframework.data.repository.query.Param("id") Long id);
    List<ProductEntity> findByFlashSaleCampaignIdOrderByIdAsc(Long id);
    long countByFlashSaleCampaignId(Long id);

    boolean existsByNameAndStatus(String name, StatusEnum status);

    List<ProductEntity> findByStatus(StatusEnum status);

    List<ProductEntity> findByCategoryId(Long categoryId);

    List<ProductEntity> findByBrandId(Long brandId);

    boolean existsByIdAndStatus(Long id, StatusEnum status);

    boolean existsByNameAndStatusAndIdNot(String name, StatusEnum status, Long id);
    ProductEntity findByNameAndStatus(String name, StatusEnum status);

    ProductEntity findByIdAndStatus(Long productId, StatusEnum status);

    List<ProductEntity> findByNameContainingOrDescriptionContainingAndStatus(String name, String description, StatusEnum status);

    // Thêm 2 methods này vào ProductRepository
    Page<ProductEntity> findByStatus(StatusEnum status, Pageable pageable);
    Page<ProductEntity> findByNameContainingIgnoreCaseAndStatus(String name, StatusEnum status, Pageable pageable);
}
