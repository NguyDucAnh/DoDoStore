package com.dodostore.dodostore.repository;
import com.dodostore.dodostore.entity.FlashSaleCampaign;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface FlashSaleCampaignRepository extends JpaRepository<FlashSaleCampaign,Long> {
    List<FlashSaleCampaign> findAllByOrderByCreatedAtDescIdDesc();
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select c from FlashSaleCampaign c where c.id=:id")
    Optional<FlashSaleCampaign> lockById(@Param("id") Long id);
}
