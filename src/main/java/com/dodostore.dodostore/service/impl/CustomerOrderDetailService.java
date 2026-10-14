package com.dodostore.dodostore.service.impl;
import com.dodostore.dodostore.entity.*;
import com.dodostore.dodostore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
@Service @RequiredArgsConstructor
public class CustomerOrderDetailService {
    private final OrderRepository orders;
    private final OrderDetailRepository details;
    public record Item(Long productId, String name, String image, String color, String size, Long quantity, Long total) {}
    public record Detail(Long id, String placedAt, String status, String statusCode, Long total, List<Item> items) {}
    @Transactional(readOnly=true)
    public Detail view(Long userId, Long orderId) {
        OrderEntity order=orders.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if(userId==null || order.getUser()==null || !userId.equals(order.getUser().getId()) || Boolean.TRUE.equals(order.getCartDraft()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        List<Item> items=details.findByOrderId(orderId).stream().map(line -> {
            ProductEntity p=line.getProduct();
            String image=p==null || p.getImages()==null || p.getImages().isEmpty() ? null : p.getImages().get(0).getImageLink();
            return new Item(p==null?null:p.getId(), p==null?"Sản phẩm không còn tồn tại":p.getName(), image,
                line.getColor(),line.getSize(),line.getQuantity(),line.getTotal());
        }).toList();
        String date=order.getDate()==null?"":java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
            .withZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).format(order.getDate().toInstant());
        return new Detail(order.getId(),date,order.getStatus()==null?"Chưa xác định":order.getStatus().getDisplayName(),
            order.getStatus()==null?"":order.getStatus().name(),order.getTotal(),items);
    }
}
