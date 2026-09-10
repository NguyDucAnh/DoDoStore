package com.dodostore.dodostore.service.impl;

import com.dodostore.dodostore.entity.CartEntity;
import com.dodostore.dodostore.entity.ProductEntity;
import com.dodostore.dodostore.repository.CartRepository;
import com.dodostore.dodostore.repository.ProductRepository;
import com.dodostore.dodostore.repository.UserRepository;
import com.dodostore.dodostore.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartRepository cartRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    @Override
    public List<CartEntity> getCart(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    @Override
    public Long getCountByUserId(Long userId) {
        return Long.valueOf(cartRepository.findByUserId(userId).size());
    }

    @Override
    public boolean addCart(Long userId, Long productId){
        if(cartRepository.findByUserIdAndProductId(userId, productId) != null) {
            return false;
        }
        CartEntity newCart = CartEntity.builder()
                .user((userRepository.findById(userId)).get())
                .product(productRepository.findById(productId).get())
                .quantity(Long.valueOf("1"))
                .build();

        cartRepository.save(newCart);
        return true;
    }

    @Override
    @Transactional
    public void removeProductToCart(Long userId, Long productId) {
        cartRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Override
    @Transactional
    public void removeCartByUserId(Long userId) {
        cartRepository.deleteByUserId(userId);
    }

    @Override
    @Transactional
    public boolean replaceQuantityProduct(Long userId, Long productId, Long quantityReplace){
        if (quantityReplace == null || (quantityReplace != 1L && quantityReplace != -1L)) return false;
        CartEntity cart = cartRepository.findForQuantityUpdate(userId, productId);
        if (cart == null) return false;
        long current = cart.getQuantity();
        if (quantityReplace == 1L && current >= cart.getProduct().getQuantity()) return false;
        if (quantityReplace == -1L && current <= 1L) return false;
        cart.setQuantity(current + quantityReplace);
        cartRepository.save(cart);
        return true;
    }
}
