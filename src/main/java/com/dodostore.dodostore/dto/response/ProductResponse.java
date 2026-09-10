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
    String description;
    String color;
    Long quantity;
    Date createDate;
    Date updateDate;
    CategoryEntity category;
    BrandEntity brand;
    List<ImageEntity> images;
}
