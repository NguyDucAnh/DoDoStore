package com.dodostore.dodostore.utils;
import java.time.LocalDateTime;
public final class FlashSale {
    private FlashSale() {}
    public static boolean active(Long discount, LocalDateTime start, LocalDateTime end, LocalDateTime now) {
        return discount != null && discount > 0 && start != null && end != null && !now.isBefore(start) && now.isBefore(end);
    }
    public static void validate(Long discount, LocalDateTime start, LocalDateTime end) {
        if (discount == null || discount == 0) return;
        if (discount < 0 || discount > 99 || start == null || end == null || !end.isAfter(start))
            throw new IllegalArgumentException("Flash sale: giảm từ 1–99%, thời gian kết thúc phải sau bắt đầu.");
    }
}
