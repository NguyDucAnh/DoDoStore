package com.dodostore.dodostore.ai.tools;

import com.dodostore.dodostore.entity.OrderEntity;
import com.dodostore.dodostore.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class PersonalInquiryTool {

    private final OrderRepository orderRepository;

    public record OrderStatusRequest(Long userId) {}
    public record OrderStatusResponse(String message, List<OrderInfo> orders) {}
    public record OrderInfo(Long orderId, String status, Long total, String date) {}

    @Bean
    @Description("Tra cứu tình trạng các đơn hàng gần đây của khách hàng")
    public Function<OrderStatusRequest, OrderStatusResponse> checkOrderStatus() {
        return request -> {
            if (request.userId() == null) {
                return new OrderStatusResponse("Cần thông tin User ID để tra cứu.", List.of());
            }

            List<OrderEntity> orders = orderRepository.findByUserId(request.userId());
            
            if (orders == null || orders.isEmpty()) {
                return new OrderStatusResponse("Không có đơn hàng nào.", List.of());
            }

            // Lấy 5 đơn hàng mới nhất
            List<OrderInfo> infos = orders.stream()
                    .sorted((o1, o2) -> o2.getDate().compareTo(o1.getDate()))
                    .limit(5)
                    .map(o -> new OrderInfo(o.getId(), o.getStatus().name(), o.getTotal(), o.getDate().toString()))
                    .collect(Collectors.toList());

            return new OrderStatusResponse("Tìm thấy " + infos.size() + " đơn hàng.", infos);
        };
    }
}
