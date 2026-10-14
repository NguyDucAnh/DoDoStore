package com.dodostore.dodostore.service.impl;

import com.dodostore.dodostore.dto.response.OrderDetailResponse;
import com.dodostore.dodostore.entity.CartEntity;
import com.dodostore.dodostore.entity.OrderDetailEntity;
import com.dodostore.dodostore.entity.OrderEntity;
import com.dodostore.dodostore.entity.ProductEntity;
import com.dodostore.dodostore.mapper.OrderDetailMapper;
import com.dodostore.dodostore.repository.OrderDetailRepository;
import com.dodostore.dodostore.repository.OrderRepository;
import com.dodostore.dodostore.repository.ProductRepository;
import com.dodostore.dodostore.service.OrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderDetailServiceImpl implements OrderDetailService{
    @Autowired
    OrderDetailRepository orderDetailRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Override
    public void AddItem(List<CartEntity> carts, Long orderId) {

        for(CartEntity cart : carts){
            OrderDetailEntity newItem = new OrderDetailEntity();

            ProductEntity product = productRepository.findById(cart.getProduct().getId()).get();

            OrderEntity order = orderRepository.findById(orderId).get();

            newItem.setQuantity(cart.getQuantity());
            newItem.setSize(cart.getSize());
            newItem.setColor(cart.getColor());
            newItem.setDiscount(product.getEffectiveDiscount());
            newItem.setPrice(product.getPrice());
            newItem.setTotal((product.getPrice() * (100 - product.getEffectiveDiscount()) / 100) * cart.getQuantity());
            newItem.setProduct(product);
            newItem.setOrder(order);

            orderDetailRepository.save(newItem);
        }

    }

    @Override
    public List<OrderDetailResponse> getItemByOrderId(Long orderId) {
        List<OrderDetailResponse> result = new ArrayList<>();

        for (OrderDetailEntity detail : orderDetailRepository.findByOrderId(orderId)){
            result.add(orderDetailMapper.toOrderDetailResponse(detail));
        }
        return result;
    }
}
