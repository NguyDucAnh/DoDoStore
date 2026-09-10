package com.dodostore.dodostore.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter @Entity
@Table(name = "payment_transaction")
public class PaymentTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "order_id") private OrderEntity order;
    @Column(nullable = false, unique = true, length = 40) private String txnRef;
    @Column(nullable = false, unique = true, length = 36) private String requestKey;
    @Column(nullable = false) private Long amount;
    private String paymentMethod = "VNPAY";
    private String status = "PENDING";
    private String responseCode;
    private String transactionStatus;
    private String vnpTransactionNo;
    private String bankCode;
    private String bankTransactionNo;
    private String cardType;
    private String payDate;
    private String secureHash;
    @Column(length = 4096) private String paymentUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
}
