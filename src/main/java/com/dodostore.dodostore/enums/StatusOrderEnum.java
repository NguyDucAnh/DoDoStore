package com.dodostore.dodostore.enums;

public enum StatusOrderEnum {
    Dang_Xu_Ly, Dang_Giao, Da_Giao, Cho_Thanh_Toan, Da_Huy;
    public String getDisplayName() {
        return switch (this) {
            case Dang_Xu_Ly -> "Đang xử lý";
            case Dang_Giao -> "Đang giao";
            case Da_Giao -> "Đã giao";
            case Cho_Thanh_Toan -> "Chờ thanh toán";
            case Da_Huy -> "Đã hủy";
        };
    }
}
