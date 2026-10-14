package com.dodostore.dodostore.repository;

import com.dodostore.dodostore.entity.OrderDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetailEntity, Long> {
    interface CampaignSales {
        Long getCampaignId();
        Long getSoldQuantity();
    }
    @org.springframework.data.jpa.repository.Query("select d.flashSaleCampaignId as campaignId, sum(d.quantity) as soldQuantity from OrderDetailEntity d where d.flashSaleCampaignId is not null and d.order.cartDraft = false and d.order.status in :statuses group by d.flashSaleCampaignId")
    List<CampaignSales> sumCampaignSales(@org.springframework.data.repository.query.Param("statuses") java.util.Collection<com.dodostore.dodostore.enums.StatusOrderEnum> statuses);

    List<OrderDetailEntity> findByOrderId(Long orderId);

    List<OrderDetailEntity> findByProductId(Long productId);
}
