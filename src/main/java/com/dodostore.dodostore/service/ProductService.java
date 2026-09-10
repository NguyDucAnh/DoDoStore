package com.dodostore.dodostore.service;

import com.dodostore.dodostore.dto.request.ProductRequest;
import com.dodostore.dodostore.dto.request.SearchRequest;
import com.dodostore.dodostore.dto.response.ProductResponse;
import com.dodostore.dodostore.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductService {
    int countProduct();

    Page<ProductResponse> getAllProductsPaginated(
            int page, int size,
            String sortField, String sortDir,
            String name, StatusEnum status
    );

    ProductResponse getProductById(Long productId);

    ProductResponse createProduct(ProductRequest productRequest);

    ProductResponse updateProduct(Long productId, ProductRequest productRequest);

    void softDeleteProduct(Long productId);

    void restoreProduct(Long productId);

    void deleteProduct(Long productId);

    Page<ProductResponse> searchProduct(SearchRequest request, int page, int size);

    List<ProductResponse> getProductSale();

    List<ProductResponse> getProductNewest();
}
