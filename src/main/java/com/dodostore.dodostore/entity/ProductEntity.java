package com.dodostore.dodostore.entity;

import com.dodostore.dodostore.enums.StatusEnum;
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
@Table(name = "product")
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "name")
    String name;

    @Column(name = "price")
    Long price;

    @Column(name = "discount")
    Long discount;

    @Column(name = "description", columnDefinition = "MEDIUMTEXT")
    String description;

    @Column(name = "color")
    String color;
    @Column(name = "sizes", length = 255)
    String sizes;

    @Column(name = "size_stock", length = 2000)
    String sizeStock;
    @Column(name = "variant_stock", columnDefinition = "TEXT")
    String variantStock;
    @Column(name = "flash_sale_discount")
    Long flashSaleDiscount;
    @Column(name = "flash_sale_start")
    java.time.LocalDateTime flashSaleStart;
    @Column(name = "flash_sale_end")
    java.time.LocalDateTime flashSaleEnd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flash_sale_campaign_id")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    FlashSaleCampaign flashSaleCampaign;

    public boolean isFlashSaleActive() {
        return flashSaleDiscount != null && flashSaleDiscount > (discount == null ? 0L : discount)
                && (flashSaleCampaign == null || flashSaleCampaign.isEnabled())
                && com.dodostore.dodostore.utils.FlashSale.active(flashSaleDiscount, flashSaleStart, flashSaleEnd,
                java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")));
    }
    public Long getEffectiveDiscount() {
        return isFlashSaleActive() ? Math.max(discount == null ? 0 : discount, flashSaleDiscount) : (discount == null ? 0L : discount);
    }
    public long stockForSize(String size) {
        return com.dodostore.dodostore.utils.SizeStock.available(sizeStock, size);
    }
    public boolean hasVariantStock() { return variantStock != null && !variantStock.isBlank(); }
    public long stockForVariant(String color, String size) {
        return hasVariantStock() ? com.dodostore.dodostore.utils.VariantStock.available(variantStock, color, size) : stockForSize(size);
    }
    public void reserveVariant(String color, String size, long amount) {
        if (!hasVariantStock()) { reserveSize(size, amount); return; }
        if (amount <= 0 || quantity == null || quantity < amount || stockForVariant(color,size) < amount)
            throw new IllegalArgumentException("Màu " + color + ", size " + size + " không đủ tồn kho: " + name);
        variantStock = com.dodostore.dodostore.utils.VariantStock.change(variantStock,color,size,-amount);
        sizeStock = com.dodostore.dodostore.utils.VariantStock.sizeTotals(variantStock);
        quantity = com.dodostore.dodostore.utils.VariantStock.total(variantStock);
    }
    public void releaseVariant(String color, String size, long amount) {
        if (!hasVariantStock()) { releaseSize(size,amount); return; }
        if (amount <= 0) throw new IllegalArgumentException("Số lượng hoàn kho không hợp lệ.");
        variantStock = com.dodostore.dodostore.utils.VariantStock.change(variantStock,color,size,amount);
        sizeStock = com.dodostore.dodostore.utils.VariantStock.sizeTotals(variantStock);
        quantity = com.dodostore.dodostore.utils.VariantStock.total(variantStock);
    }
    public void reserveSize(String size, long amount) {
        if (amount <= 0 || quantity == null || quantity < amount || stockForSize(size) < amount)
            throw new IllegalArgumentException("Size " + size + " không đủ tồn kho: " + name);
        sizeStock = com.dodostore.dodostore.utils.SizeStock.change(sizeStock, size, -amount);
        quantity -= amount;
    }
    public void releaseSize(String size, long amount) {
        // Legacy orders may predate allocation of stock to sizes.
        if (sizeStock != null && !sizeStock.isBlank())
            sizeStock = com.dodostore.dodostore.utils.SizeStock.change(sizeStock, size, amount);
        quantity = Math.addExact(quantity == null ? 0 : quantity, amount);
    }

    @Column(name = "quantity")
    Long quantity;

    @Column(name = "create_date")
    Date createDate;

    @Column(name = "update_date")
    Date updateDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_product")
    StatusEnum status;

    @Builder.Default
    @Column(name = "quantity_sell")
    Long quantitySell = 0L;

    @ManyToOne
    @JoinColumn(name = "category_id")
    CategoryEntity category;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    BrandEntity brand;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    List<ImageEntity> images;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    List<OrderDetailEntity> orderDetails;
    public List<String> getAvailableSizes() {
        return com.dodostore.dodostore.utils.ProductSizes.parse(sizes);
    }
    public List<String> getAvailableColors() {
        return com.dodostore.dodostore.utils.ProductColors.parse(color);
    }
}