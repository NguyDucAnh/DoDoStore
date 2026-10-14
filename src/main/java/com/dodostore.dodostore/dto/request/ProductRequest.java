package com.dodostore.dodostore.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequest {
    Long id;
    String name;
    Long price;
    Long discount;
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
    Long categoryId;
    Long brandId;
}
