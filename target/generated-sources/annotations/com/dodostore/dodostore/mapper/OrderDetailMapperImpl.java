package com.dodostore.dodostore.mapper;

import com.dodostore.dodostore.dto.response.OrderDetailResponse;
import com.dodostore.dodostore.entity.OrderDetailEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-10T20:52:16+0700",
    comments = "version: 1.6.0, compiler: javac, environment: Java 21.0.4 (Oracle Corporation)"
)
@Component
public class OrderDetailMapperImpl implements OrderDetailMapper {

    @Override
    public OrderDetailResponse toOrderDetailResponse(OrderDetailEntity order) {
        if ( order == null ) {
            return null;
        }

        OrderDetailResponse.OrderDetailResponseBuilder orderDetailResponse = OrderDetailResponse.builder();

        orderDetailResponse.id( order.getId() );
        orderDetailResponse.quantity( order.getQuantity() );
        orderDetailResponse.price( order.getPrice() );
        orderDetailResponse.discount( order.getDiscount() );
        orderDetailResponse.total( order.getTotal() );
        orderDetailResponse.order( order.getOrder() );
        orderDetailResponse.product( order.getProduct() );

        return orderDetailResponse.build();
    }
}
