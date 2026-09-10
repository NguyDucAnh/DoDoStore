package com.dodostore.dodostore.mapper;

import com.dodostore.dodostore.dto.response.OrderResponse;
import com.dodostore.dodostore.entity.OrderDetailEntity;
import com.dodostore.dodostore.entity.OrderEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-10T20:52:16+0700",
    comments = "version: 1.6.0, compiler: javac, environment: Java 21.0.4 (Oracle Corporation)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public OrderResponse toOrderResponse(OrderEntity order) {
        if ( order == null ) {
            return null;
        }

        OrderResponse.OrderResponseBuilder orderResponse = OrderResponse.builder();

        orderResponse.id( order.getId() );
        orderResponse.date( order.getDate() );
        orderResponse.total( order.getTotal() );
        orderResponse.quantity( order.getQuantity() );
        orderResponse.phone( order.getPhone() );
        orderResponse.address( order.getAddress() );
        orderResponse.status( order.getStatus() );
        orderResponse.user( order.getUser() );
        List<OrderDetailEntity> list = order.getItems();
        if ( list != null ) {
            orderResponse.items( new ArrayList<OrderDetailEntity>( list ) );
        }

        return orderResponse.build();
    }
}
