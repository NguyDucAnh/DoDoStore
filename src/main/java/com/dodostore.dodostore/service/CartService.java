package com.dodostore.dodostore.service;

import com.dodostore.dodostore.entity.CartEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CartService {
    // Get All Cart
    List<CartEntity> getCart(Long userId);

    // Find Count By UserId
    Long getCountByUserId(Long userId);

    // Add Product to Cart
    boolean addCart(Long userId, Long productId, String size, String color);

    // Remove Product
    void removeProductToCart(Long userId, Long cartId);

    // Delete Cart When CheckOut
    void removeCartByUserId(Long userId);

    boolean setQuantity(Long userId, Long cartId, Long quantity);

    // Replace quantity product
    boolean replaceQuantityProduct(Long userId, Long cartId, Long quantityReplace);
}
