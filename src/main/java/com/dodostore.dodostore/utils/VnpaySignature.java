package com.dodostore.dodostore.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class VnpaySignature {
    private VnpaySignature() {}
    public static String canonical(Map<String, String> params) {
        return new TreeMap<>(params).entrySet().stream()
            .filter(e -> e.getKey().startsWith("vnp_") && !e.getKey().equals("vnp_SecureHash")
                && !e.getKey().equals("vnp_SecureHashType") && e.getValue() != null && !e.getValue().isEmpty())
            .map(e -> encode(e.getKey()) + "=" + encode(e.getValue())).collect(Collectors.joining("&"));
    }
    private static String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
    public static String sign(Map<String, String> params, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(canonical(params).getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.GeneralSecurityException e) { throw new IllegalStateException("Cannot sign payment", e); }
    }
    public static boolean valid(Map<String, String> params, String secret) {
        String hash = params.get("vnp_SecureHash");
        return hash != null && hash.matches("[a-fA-F0-9]{128}") && MessageDigest.isEqual(
            sign(params, secret).getBytes(StandardCharsets.US_ASCII), hash.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII));
    }
}
