package com.dodostore.dodostore;

import com.dodostore.dodostore.utils.VnpaySignature;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class VnpaySignatureTest {
    @Test void sortsEncodesAndExcludesSignature() {
        Map<String,String> data = new HashMap<>();
        data.put("vnp_B", "a+b c"); data.put("vnp_A", "100"); data.put("vnp_empty", "");
        data.put("vnp_SecureHash", "ignore"); data.put("vnp_SecureHashType", "ignore");
        assertEquals("vnp_A=100&vnp_B=a%2Bb+c", VnpaySignature.canonical(data));
    }
    @Test void rejectsTamperedAmountAndMissingSignature() {
        Map<String,String> data = new HashMap<>(Map.of("vnp_Amount", "1000000", "vnp_TxnRef", "abc"));
        assertFalse(VnpaySignature.valid(data, "test-secret"));
        data.put("vnp_SecureHash", VnpaySignature.sign(data, "test-secret"));
        assertTrue(VnpaySignature.valid(data, "test-secret"));
        data.put("vnp_Amount", "1");
        assertFalse(VnpaySignature.valid(data, "test-secret"));
    }
}
