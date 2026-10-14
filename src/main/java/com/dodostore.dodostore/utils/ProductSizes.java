package com.dodostore.dodostore.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class ProductSizes {
    private ProductSizes() {}
    public static List<String> parse(String sizes) {
        if (sizes == null || sizes.isBlank()) return List.of();
        return Arrays.stream(sizes.split(",")).map(String::trim)
                .filter(s -> !s.isEmpty()).map(s -> s.toUpperCase(Locale.ROOT)).distinct().toList();
    }
    public static String normalize(String sizes) {
        List<String> values = parse(sizes);
        if (values.isEmpty() || values.stream().anyMatch(s -> !s.matches("[A-Z0-9][A-Z0-9 ._/-]{0,29}")))
            throw new IllegalArgumentException("Nhập size hợp lệ, cách nhau bằng dấu phẩy (ví dụ S,M,L hoặc 38,39,40).");
        String result = String.join(",", values);
        if (result.length() > 255) throw new IllegalArgumentException("Danh sách size quá dài.");
        return result;
    }
    public static void requireSelection(com.dodostore.dodostore.entity.CartEntity cart) {
        if (ProductColors.select(cart.getProduct().getColor(), cart.getColor()) == null)
            throw new IllegalArgumentException("Vui lòng xóa và chọn lại màu cho sản phẩm: " + cart.getProduct().getName());
        if (cart.getSize() == null || !cart.getProduct().getAvailableSizes().contains(cart.getSize()))
            throw new IllegalArgumentException("Vui lòng xóa và chọn lại size cho sản phẩm: " + cart.getProduct().getName());
    }
}
