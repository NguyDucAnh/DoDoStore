package com.dodostore.dodostore.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.dodostore.dodostore.enums.FlashSaleScope;
import java.time.LocalDateTime;
@Getter @Setter @Entity @Table(name="flash_sale_campaign")
public class FlashSaleCampaign {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private Long version;
    @Column(nullable=false,length=120) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private FlashSaleScope scope;
    @Column(nullable=false) private Long discount;
    @Column(nullable=false) private LocalDateTime startsAt;
    @Column(nullable=false) private LocalDateTime endsAt;
    @Column(nullable=false) private boolean enabled=true;
    @Column(nullable=false) private LocalDateTime createdAt;
    @Column(nullable=false) private LocalDateTime updatedAt;
    @Transient private long productCount;
    @Transient private long soldQuantity;
    public boolean isResumable() {
        return !enabled && endsAt != null && endsAt.isAfter(LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")));
    }
    public String getState() {
        if (!enabled) return "STOPPED";
        LocalDateTime now=LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        if (!now.isBefore(endsAt)) return "ENDED";
        return now.isBefore(startsAt) ? "SCHEDULED" : "ACTIVE";
    }
    public String getStateLabel() {
        return switch(getState()) { case "STOPPED" -> "Đã dừng"; case "ENDED" -> "Đã kết thúc"; case "SCHEDULED" -> "Sắp diễn ra"; default -> "Đang diễn ra"; };
    }
}
