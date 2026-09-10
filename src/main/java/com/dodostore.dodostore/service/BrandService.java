package com.dodostore.dodostore.service;

import com.dodostore.dodostore.dto.request.BrandRequest;
import com.dodostore.dodostore.dto.response.BrandResponse;
import com.dodostore.dodostore.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public interface BrandService {
    Page<BrandResponse> getAllPaginated(
            int page, int size,
            String sortField, String sortDir,
            String name, StatusEnum status
    );

    BrandResponse getBrandById(Long brandId);

    BrandResponse createBrand(BrandRequest brandRequest, MultipartFile file);

    BrandResponse updateBrand(Long brandId, BrandRequest brandRequest, MultipartFile file);

    void softDeleteBrand(Long brandId);

    void restoreBrand(Long brandId);

    void deleteBrand(Long brandId);

    void updateImage(Long brandId, String image);

    void deleteImage(Long brandId);
}
