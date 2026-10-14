package com.dodostore.dodostore.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.*;
import com.dodostore.dodostore.enums.FlashSaleScope;
@Data
public class FlashSaleCampaignRequest {
    private Long id;
    private Long version;
    @NotBlank(message="Vui lòng nhập tên chiến dịch.") @Size(max=120,message="Tên chiến dịch tối đa 120 ký tự.")
    private String name;
    @NotNull(message="Vui lòng chọn phạm vi áp dụng.") private FlashSaleScope scope=FlashSaleScope.SELECTED;
    @NotNull(message="Vui lòng nhập mức giảm.") @Min(value=1,message="Mức giảm từ 1 đến 99%.") @Max(value=99,message="Mức giảm từ 1 đến 99%.")
    private Long discount;
    @NotNull(message="Vui lòng nhập thời gian bắt đầu.")
    @org.springframework.format.annotation.DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm") private LocalDateTime startsAt;
    @NotNull(message="Vui lòng nhập thời gian kết thúc.")
    @org.springframework.format.annotation.DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm") private LocalDateTime endsAt;
    private List<Long> productIds=new ArrayList<>();
}
