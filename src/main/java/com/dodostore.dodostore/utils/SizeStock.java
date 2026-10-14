package com.dodostore.dodostore.utils;
import java.util.*;
import java.util.stream.Collectors;
public final class SizeStock {
    private SizeStock() {}
    public static Map<String, Long> parse(String text) {
        Map<String, Long> values = new LinkedHashMap<>();
        if (text == null || text.isBlank()) return values;
        for (String part : text.split(",", -1)) {
            String[] pair = part.trim().split(":", -1);
            if (pair.length != 2 || !pair[1].trim().matches("[0-9]+"))
                throw new IllegalArgumentException("Tồn kho size phải có dạng S:10,M:20,L:15; không được nhập số âm.");
            String size = pair[0].trim().toUpperCase(Locale.ROOT);
            long value;
            try { value = Long.parseLong(pair[1].trim()); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("Số lượng tồn kho quá lớn."); }
            if (size.isEmpty() || values.putIfAbsent(size, value) != null)
                throw new IllegalArgumentException("Size trống hoặc trùng lặp.");
        }
        return values;
    }
    public static String format(Map<String, Long> values) {
        return values.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining(","));
    }
    public static String validate(String sizes, String text) {
        Map<String, Long> values = parse(text);
        if (values.isEmpty() || !values.keySet().equals(new HashSet<>(ProductSizes.parse(sizes))))
            throw new IllegalArgumentException("Nhập tồn kho cho đầy đủ các size đang bán, không thêm size ngoài danh sách.");
        total(text);
        String result = format(values);
        if (result.length() > 2000) throw new IllegalArgumentException("Danh sách tồn kho quá dài.");
        return result;
    }
    public static long total(String text) {
        long total = 0;
        for (long v : parse(text).values()) total = Math.addExact(total, v);
        return total;
    }
    public static long available(String text, String size) { return parse(text).getOrDefault(size, 0L); }
    public static String change(String text, String size, long delta) {
        Map<String, Long> values = parse(text);
        long next = Math.addExact(values.getOrDefault(size, 0L), delta);
        if (next < 0) throw new IllegalArgumentException("Size " + size + " không đủ tồn kho.");
        values.put(size, next);
        return format(values);
    }
}
