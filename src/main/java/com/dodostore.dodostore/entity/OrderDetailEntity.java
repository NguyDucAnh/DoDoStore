package com.dodostore.dodostore.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "order_detail")
public class OrderDetailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "quantity")
    Long quantity;
    @Column(name = "size", length = 30)
    String size;
    @Column(name = "color", length = 60)
    String color;

    @Column(name = "price")
    Long price;

    @Column(name = "discount")
    Long discount;

    @Column(name = "total")
    Long total;

    // Snapshot of the promotion applied at checkout; legacy orders remain null.
    @Column(name = "flash_sale_campaign_id")
    Long flashSaleCampaignId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    ProductEntity product;

    @ManyToOne
    @JoinColumn(name = "order_id")
    OrderEntity order;
}
