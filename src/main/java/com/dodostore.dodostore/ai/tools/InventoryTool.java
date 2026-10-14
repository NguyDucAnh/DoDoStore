package com.dodostore.dodostore.ai.tools;

import com.dodostore.dodostore.entity.ProductEntity;
import com.dodostore.dodostore.enums.StatusEnum;
import com.dodostore.dodostore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class InventoryTool {

    private final ProductRepository productRepository;

    public record InventoryRequest(String productName, String color) {}
    public record InventoryResponse(String message, List<ProductInfo> products) {}
    public record ProductInfo(Long id, String name, String color, Long quantity, Long price, Long discount, Long salePrice, String url) {}
    private ProductInfo info(ProductEntity p) {
        long price=p.getPrice()==null?0:p.getPrice();
        long discount=p.getEffectiveDiscount();
        return new ProductInfo(p.getId(),p.getName(),p.getColor(),p.getQuantity(),p.getPrice(),discount,
            price * (100-discount)/100,"/home/product/"+p.getId());
    }
    public record RecommendationRequest(String keyword, Long maxPrice) {}

    @Bean
    @Description("Tra cứu thông tin tồn kho, màu sắc và giá của sản phẩm trong cửa hàng")
    public Function<InventoryRequest, InventoryResponse> checkStock() {
        return request -> {
            String name = request.productName() != null ? request.productName() : "";
            Page<ProductEntity> productPage = productRepository.findByNameContainingIgnoreCaseAndStatus(
                    name, StatusEnum.Active, PageRequest.of(0, 50)
            );
            
            List<ProductEntity> products = productPage.getContent();
            
            if (request.color() != null && !request.color().isBlank()) {
                products = products.stream()
                        .filter(p -> p.getColor() != null && p.getColor().toLowerCase().contains(request.color().toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (products.isEmpty()) {
                return new InventoryResponse("Không tìm thấy sản phẩm phù hợp với yêu cầu.", List.of());
            }

            List<ProductInfo> infos = products.stream()
                    .map(this::info)
                    .collect(Collectors.toList());

            return new InventoryResponse("Tìm thấy " + products.size() + " sản phẩm.", infos);
        };
    }

    @Bean
    @Description("Gợi ý sản phẩm dựa trên từ khóa, mức giá tối đa, hoặc yêu cầu sở thích của khách hàng")
    public Function<RecommendationRequest, InventoryResponse> recommendProducts() {
        return request -> {
            String keyword = request.keyword() != null ? request.keyword() : "";
            List<ProductEntity> products = productRepository.findByNameContainingOrDescriptionContainingAndStatus(
                    keyword, keyword, StatusEnum.Active
            );
            
            products = products.stream().filter(p -> p.getStatus() == StatusEnum.Active).toList();
            if (request.maxPrice() != null && request.maxPrice() > 0) {
                products = products.stream()
                        .filter(p -> p.getPrice() != null && p.getPrice() * (100 - p.getEffectiveDiscount()) / 100 <= request.maxPrice())
                        .collect(Collectors.toList());
            }

            // Giới hạn số lượng gợi ý
            if (products.size() > 10) {
                products = products.subList(0, 10);
            }

            if (products.isEmpty()) {
                return new InventoryResponse("Không tìm thấy sản phẩm nào phù hợp để gợi ý theo yêu cầu của bạn.", List.of());
            }

            List<ProductInfo> infos = products.stream()
                    .map(this::info)
                    .collect(Collectors.toList());

            return new InventoryResponse("Tìm thấy " + products.size() + " sản phẩm gợi ý.", infos);
        };
    }
}
