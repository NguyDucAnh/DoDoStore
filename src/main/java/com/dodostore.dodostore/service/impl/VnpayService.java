package com.dodostore.dodostore.service.impl;

import com.dodostore.dodostore.config.VnpayProperties;
import com.dodostore.dodostore.entity.*;
import com.dodostore.dodostore.enums.*;
import com.dodostore.dodostore.repository.*;
import com.dodostore.dodostore.utils.VnpaySignature;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service @RequiredArgsConstructor
public class VnpayService {
    private final VnpayProperties config;
    private final PaymentTransactionRepository payments;
    private final CartRepository carts;
    private final OrderDetailRepository details;
    @PersistenceContext private EntityManager em;
    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Transactional
    public String create(Long userId, Long addressId, String requestKey, String ip) {
        config.requireConfigured();
        UUID.fromString(requestKey);
        UserEntity user = em.find(UserEntity.class, userId, LockModeType.PESSIMISTIC_WRITE);
        if (user == null || user.getStatus() != StatusEnum.Active) throw new IllegalArgumentException("Tài khoản không hợp lệ.");
        var previous = payments.findByRequestKey(requestKey);
        if (previous.isPresent()) {
            PaymentTransaction p = previous.get();
            if (!p.getOrder().getUser().getId().equals(userId)) throw new IllegalArgumentException("Giao dịch không thuộc tài khoản.");
            if (!p.getStatus().equals("PENDING") || p.getExpiresAt().isBefore(LocalDateTime.now(ZONE)))
                throw new IllegalArgumentException("Giao dịch đã kết thúc hoặc hết hạn. Kiểm tra lịch sử thanh toán.");
            return p.getPaymentUrl();
        }
        AddressEntity address = em.find(AddressEntity.class, addressId);
        if (address == null || !address.getUser().getId().equals(userId)
                || address.getAddress() == null || address.getPhone() == null)
            throw new IllegalArgumentException("Địa chỉ nhận hàng không hợp lệ.");
        List<CartEntity> items = new ArrayList<>(carts.findByUserId(userId));
        if (items.isEmpty()) throw new IllegalArgumentException("Giỏ hàng trống.");
        items.sort(Comparator.comparing(c -> c.getProduct().getId()));
        OrderEntity order = new OrderEntity();
        order.setUser(user); order.setDate(new Date()); order.setAddress(address.getAddress());
        order.setPhone(address.getPhone()); order.setStatus(StatusOrderEnum.Cho_Thanh_Toan);
        order.setQuantity((long) items.size()); em.persist(order);
        long total = 0;
        for (CartEntity cart : items) {
            ProductEntity product = cart.getProduct();
            em.refresh(product, LockModeType.PESSIMISTIC_WRITE);
            long quantity = cart.getQuantity() == null ? 0 : cart.getQuantity();
            long discount = product.getDiscount() == null ? 0 : product.getDiscount();
            if (quantity <= 0 || product.getStatus() != StatusEnum.Active || product.getQuantity() < quantity
                || product.getPrice() == null || product.getPrice() <= 0 || discount < 0 || discount > 100)
                throw new IllegalArgumentException("Sản phẩm không hợp lệ hoặc không đủ tồn kho: " + product.getName());
            long line = Math.multiplyExact(Math.multiplyExact(product.getPrice(), 100 - discount), quantity) / 100;
            OrderDetailEntity detail = new OrderDetailEntity();
            detail.setOrder(order); detail.setProduct(product); detail.setQuantity(quantity);
            detail.setPrice(product.getPrice()); detail.setDiscount(discount); detail.setTotal(line); em.persist(detail);
            total = Math.addExact(total, line);
            // Reserve stock now. Sold quantity is only increased after a verified successful IPN.
            product.setQuantity(product.getQuantity() - quantity);
            em.remove(cart);
        }
        if (total <= 0 || Math.multiplyExact(total, 100) > 999999999999L)
            throw new IllegalArgumentException("Tổng tiền không hợp lệ.");
        order.setTotal(total);
        LocalDateTime now = LocalDateTime.now(ZONE);
        PaymentTransaction payment = new PaymentTransaction();
        payment.setOrder(order); payment.setAmount(total); payment.setRequestKey(requestKey);
        payment.setTxnRef(UUID.randomUUID().toString().replace("-", ""));
        payment.setCreatedAt(now); payment.setUpdatedAt(now); payment.setExpiresAt(now.plusMinutes(15));
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0"); params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", config.getTmnCode()); params.put("vnp_Amount", Long.toString(total * 100));
        params.put("vnp_CurrCode", "VND"); params.put("vnp_TxnRef", payment.getTxnRef());
        params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getId()); params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn"); params.put("vnp_ReturnUrl", config.getReturnUrl());
        params.put("vnp_IpAddr", ip); params.put("vnp_CreateDate", now.format(FORMAT));
        params.put("vnp_ExpireDate", payment.getExpiresAt().format(FORMAT));
        payment.setPaymentUrl(config.getPayUrl() + "?" + VnpaySignature.canonical(params)
            + "&vnp_SecureHash=" + VnpaySignature.sign(params, config.getHashSecret()));
        payments.save(payment);
        return payment.getPaymentUrl();
    }

    public boolean authentic(Map<String, String> params) {
        config.requireConfigured();
        return config.getTmnCode().equals(params.get("vnp_TmnCode")) && VnpaySignature.valid(params, config.getHashSecret());
    }
    public static Map<String, String> reply(String code, String message) {
        return Map.of("RspCode", code, "Message", message);
    }
    @Transactional
    public Map<String, String> ipn(Map<String, String> params) {
        if (!authentic(params)) return reply("97", "Invalid signature");
        PaymentTransaction p = payments.lockByRef(params.get("vnp_TxnRef")).orElse(null);
        if (p == null) return reply("01", "Order not found");
        if (!Long.toString(p.getAmount() * 100).equals(params.get("vnp_Amount"))) return reply("04", "Invalid amount");
        if (!"PENDING".equals(p.getStatus())) return reply("02", "Order already confirmed");
        String response = params.get("vnp_ResponseCode"), status = params.get("vnp_TransactionStatus");
        if (response == null || status == null) return reply("99", "Missing result");
        // Intermediate/reversal states need reconciliation, not automatic stock release.
        if (!"00".equals(status) && !"02".equals(status)) return reply("99", "Result needs reconciliation");
        if ("00".equals(status) && !"00".equals(response)) return reply("99", "Inconsistent result");
        boolean success = "00".equals(response) && "00".equals(status);
        p.setStatus(success ? "SUCCESS" : "24".equals(response) ? "CANCELLED" : "15".equals(response) ? "EXPIRED" : "FAILED");
        p.setResponseCode(response); p.setTransactionStatus(status);
        p.setVnpTransactionNo(params.get("vnp_TransactionNo")); p.setBankCode(params.get("vnp_BankCode"));
        p.setBankTransactionNo(params.get("vnp_BankTranNo")); p.setCardType(params.get("vnp_CardType"));
        p.setPayDate(params.get("vnp_PayDate")); p.setSecureHash(params.get("vnp_SecureHash"));
        p.setUpdatedAt(LocalDateTime.now(ZONE));
        em.refresh(p.getOrder(), LockModeType.PESSIMISTIC_WRITE);
        List<OrderDetailEntity> lines = details.findByOrderId(p.getOrder().getId());
        lines.sort(Comparator.comparing(d -> d.getProduct().getId()));
        for (OrderDetailEntity line : lines) {
            ProductEntity product = line.getProduct(); em.refresh(product, LockModeType.PESSIMISTIC_WRITE);
            if (success) product.setQuantitySell(Math.addExact(product.getQuantitySell() == null ? 0 : product.getQuantitySell(), line.getQuantity()));
            else product.setQuantity(Math.addExact(product.getQuantity(), line.getQuantity()));
        }
        p.getOrder().setStatus(success ? StatusOrderEnum.Dang_Xu_Ly : StatusOrderEnum.Da_Huy);
        return reply("00", "Confirm Success");
    }
    @Transactional(readOnly = true)
    public Map<String, Object> result(Map<String, String> params) {
        if (!authentic(params)) throw new IllegalArgumentException("Chữ ký thanh toán không hợp lệ.");
        PaymentTransaction p = payments.findByTxnRef(params.get("vnp_TxnRef")).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch."));
        if (!Long.toString(p.getAmount() * 100).equals(params.get("vnp_Amount"))) throw new IllegalArgumentException("Số tiền không khớp.");
        String message = switch (p.getStatus()) {
            case "SUCCESS" -> "Thanh toán thành công. Đơn hàng đang được xử lý.";
            case "FAILED", "CANCELLED", "EXPIRED" -> "Thanh toán thất bại, bị hủy hoặc hết hạn. Hàng đã được hoàn tồn.";
            default -> "Đang chờ VNPay xác nhận qua IPN. Không thanh toán lại nếu tài khoản đã bị trừ tiền.";
        };
        return Map.of("message", message, "orderId", p.getOrder().getId(), "amount", p.getAmount(), "status", p.getStatus(), "txnRef", p.getTxnRef());
    }
}
