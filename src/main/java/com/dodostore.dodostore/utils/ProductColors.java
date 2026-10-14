package com.dodostore.dodostore.utils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public final class ProductColors {
    private ProductColors() {}
    private static String clean(String value) {
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFC);
    }
    public static List<String> parse(String colors) {
        List<String> result = new ArrayList<>();
        if (colors == null || colors.isBlank()) return result;
        for (String raw : colors.split(",")) {
            String color = clean(raw);
            if (!color.isEmpty() && result.stream().noneMatch(c -> c.equalsIgnoreCase(color)))
                result.add(color);
        }
        return result;
    }
    public static String normalize(String colors) {
        List<String> values = parse(colors);
        if (values.isEmpty() || values.stream().anyMatch(c -> c.length() > 60 || c.chars().anyMatch(Character::isISOControl)))
            throw new IllegalArgumentException("Nhập màu sắc hợp lệ, tối đa 60 ký tự mỗi màu, cách nhau bằng dấu phẩy.");
        String result = String.join(",", values);
        if (result.length() > 255) throw new IllegalArgumentException("Danh sách màu tối đa 255 ký tự.");
        return result;
    }
    // Use the product's canonical spelling for the cart and order snapshot.
    public static String select(String colors, String selected) {
        if (selected == null) return null;
        return parse(colors).stream().filter(c -> c.equalsIgnoreCase(clean(selected))).findFirst().orElse(null);
    }
}
