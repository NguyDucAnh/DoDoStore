package com.dodostore.dodostore.repository.customer;

import com.dodostore.dodostore.dto.request.SearchRequest;
import com.dodostore.dodostore.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepositoryCustomer {
    Page<ProductEntity> search(SearchRequest request, Pageable pageable);
}
