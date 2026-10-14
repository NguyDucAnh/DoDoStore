package com.dodostore.dodostore.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

public final class VariantStock {
    private static final ObjectMapper JSON = new ObjectMapper();
    private VariantStock() {}
    public static Map<String, Map<String, Long>> parse(String text) {
        if (text == null || text.isBlank()) return new LinkedHashMap<>();
        try {
            var root = JSON.readTree(text);
            if (!root.isObject()) throw new IllegalArgumentException();
            root.fields().forEachRemaining(color -> {
                if (!color.getValue().isObject()) throw new IllegalArgumentException();
                color.getValue().fields().forEachRemaining(size -> {
                    var value = size.getValue();
                    if (!value.isIntegralNumber() || !value.canConvertToLong() || value.longValue() < 0)
                        throw new IllegalArgumentException();
                });
            });
            return JSON.convertValue(root, new TypeReference<LinkedHashMap<String, Map<String, Long>>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Tồn kho màu–size phải là số nguyên không âm.");
        }
    }
    public static String format(Map<String, Map<String, Long>> values) {
        try { return JSON.writeValueAsString(values); }
        catch (Exception e) { throw new IllegalArgumentException("Không thể lưu tồn kho.", e); }
    }
    public static String validate(String colors, String sizes, String text) {
        var values = parse(text);
        if (!values.keySet().equals(new HashSet<>(ProductColors.parse(colors))) || values.isEmpty())
            throw new IllegalArgumentException("Nhập tồn kho cho đầy đủ các màu.");
        var expected = new HashSet<>(ProductSizes.parse(sizes));
        if (expected.isEmpty() || values.values().stream().anyMatch(v -> !v.keySet().equals(expected)))
            throw new IllegalArgumentException("Nhập tồn kho cho từng size của mỗi màu; nhập 0 nếu không bán.");
        total(text);
        return format(values);
    }
    public static long available(String text, String color, String size) {
        return parse(text).getOrDefault(color, Collections.emptyMap()).getOrDefault(size, 0L);
    }
    public static String sizeTotals(String text) {
        Map<String, Long> totals = new LinkedHashMap<>();
        parse(text).values().forEach(sizes -> sizes.forEach((s,q) -> totals.merge(s,q,Math::addExact)));
        return SizeStock.format(totals);
    }
    public static long total(String text) { return SizeStock.total(sizeTotals(text)); }
    public static String change(String text, String color, String size, long delta) {
        var values = parse(text);
        // Keep the original pair when restoring an order whose option was later removed.
        var sizes = values.computeIfAbsent(color, c -> new LinkedHashMap<>());
        long next = Math.addExact(sizes.getOrDefault(size,0L),delta);
        if (next < 0) throw new IllegalArgumentException("Màu " + color + ", size " + size + " không đủ tồn kho.");
        sizes.put(size,next);
        return format(values);
    }
}
