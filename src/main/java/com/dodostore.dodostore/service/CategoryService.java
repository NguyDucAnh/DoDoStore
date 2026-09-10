package com.dodostore.dodostore.service;

import com.dodostore.dodostore.dto.request.CategoryRequest;
import com.dodostore.dodostore.dto.response.CategoryResponse;
import com.dodostore.dodostore.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface CategoryService {

    Page<CategoryResponse> getAllCategoriesPaginated(
            int page, int size,
            String sortField, String sortDir,
            String name, StatusEnum status
    );

    CategoryResponse getCategoryById(Long categoryId);

    CategoryResponse createCategory(CategoryRequest categoryRequest,  MultipartFile file);

    CategoryResponse updateCategory(Long categoryId, CategoryRequest categoryRequest, MultipartFile file);

    void softDeleteCategory(Long categoryId);

    void restoreCategory(Long categoryId);

    void deleteCategory(Long categoryId);

    void updateImage(Long categoryId, String image);

    void deleteImage(Long categoryId);
}
