package com.dodostore.dodostore.dto.response;

import com.dodostore.dodostore.entity.BrandEntity;
import com.dodostore.dodostore.entity.CategoryEntity;
import com.dodostore.dodostore.entity.ImageEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {
    Long id;
    String name;
    Long price;
    Long discount;
    Long baseDiscount;
    boolean flashSaleActive;
    String description;
    String color;
    String sizes;
    String sizeStock;
    String variantStock;
    Long flashSaleDiscount;
    @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    java.time.LocalDateTime flashSaleStart;
    @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    java.time.LocalDateTime flashSaleEnd;

    Long quantity;
    Date createDate;
    Date updateDate;
    CategoryEntity category;
    BrandEntity brand;
    List<ImageEntity> images;
    public long stockForSize(String size) { return com.dodostore.dodostore.utils.SizeStock.available(sizeStock, size); }
    public List<String> getAvailableSizes() {
        return com.dodostore.dodostore.utils.ProductSizes.parse(sizes);
    }
    public List<String> getAvailableColors() {
        return com.dodostore.dodostore.utils.ProductColors.parse(color);
    }
}
