package com.dodostore.dodostore.dto.request;

import com.dodostore.dodostore.enums.SortEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchRequest {
    String name;
    Long minPrice;
    Long maxPrice;
    Integer page;
    Integer size;
    List<String> brands;
    List<String> categories;
    SortEnum typeSort;
}
