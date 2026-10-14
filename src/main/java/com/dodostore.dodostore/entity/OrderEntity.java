package com.dodostore.dodostore.entity;

import com.dodostore.dodostore.enums.StatusOrderEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "date")
    Date date;

    @Column(name = "total")
    Long total;

    @Column(name = "quantity_product")
    Long quantity;

    @Column(name = "address")
    String address;

    @Column(name = "phone")
    String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_order")
    StatusOrderEnum status;

    // True only while this order mirrors the customer's cart.
    // Nullable for compatibility with existing orders.
    @Column(name = "cart_draft")
    Boolean cartDraft;

    @ManyToOne
    @JoinColumn(name = "user_id")
    UserEntity user;

    @OneToMany(mappedBy = "order",fetch = FetchType.LAZY)
    List<OrderDetailEntity> items;
}

