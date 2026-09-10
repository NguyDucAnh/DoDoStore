package com.dodostore.dodostore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vnpay")
public class VnpayProperties {
    private String tmnCode = "YOUR_TMN_CODE";
    private String hashSecret = "YOUR_HASH_SECRET";
    private String payUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private String returnUrl = "http://localhost:8080/payment/vnpay/return";
    private String ipnUrl = ""; // Register externally with VNPay; not a payment URL parameter.
    public void requireConfigured() {
        if (tmnCode == null || hashSecret == null || tmnCode.isBlank() || hashSecret.isBlank()
                || tmnCode.startsWith("YOUR_") || hashSecret.startsWith("YOUR_"))
            throw new IllegalStateException("Vui lòng cấu hình vnpay.tmn-code và vnpay.hash-secret.");
    }
}
