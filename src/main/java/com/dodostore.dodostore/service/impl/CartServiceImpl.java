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
    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    @Autowired
    private CartOrderService cartOrders;

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
    @Transactional
    public boolean addCart(Long userId, Long productId, String size, String color) {
        // Serialize changes for the same owner; each size is a separate cart line.
        entityManager.find(com.dodostore.dodostore.entity.UserEntity.class, userId,
                jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        ProductEntity product = productRepository.findById(productId).orElse(null);
        String selected = size == null ? "" : size.trim().toUpperCase(java.util.Locale.ROOT);
        String selectedColor = product == null ? null : com.dodostore.dodostore.utils.ProductColors.select(product.getColor(), color);
        if (product == null || product.getStatus() != com.dodostore.dodostore.enums.StatusEnum.Active
                || selectedColor == null || !product.getAvailableSizes().contains(selected)) return false;
        long total = cartRepository.findByUserId(userId).stream()
                .filter(c -> c.getProduct().getId().equals(productId)).mapToLong(CartEntity::getQuantity).sum();
        if (product.getQuantity() == null || total >= product.getQuantity()) return false;
        long sameSize = cartRepository.findByUserId(userId).stream()
                .filter(c -> c.getProduct().getId().equals(productId) && selected.equals(c.getSize())
                        && (!product.hasVariantStock() || java.util.Objects.equals(selectedColor, c.getColor())))
                .mapToLong(CartEntity::getQuantity).sum();
        if (sameSize >= product.stockForVariant(selectedColor, selected)) return false;
        CartEntity cart = cartRepository.findByUserIdAndProductIdAndSizeAndColor(userId, productId, selected, selectedColor);
        if (cart == null) {
            cart = CartEntity.builder().user(userRepository.findById(userId).orElseThrow())
                    .product(product).color(selectedColor).size(selected).quantity(1L).build();
        } else cart.setQuantity(cart.getQuantity() + 1);
        cartRepository.saveAndFlush(cart);
        cartOrders.sync(userId);
        return true;
    }

    @Override
    @Transactional
    public void removeProductToCart(Long userId, Long cartId) {
        entityManager.find(com.dodostore.dodostore.entity.UserEntity.class, userId, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        cartRepository.deleteByUserIdAndId(userId, cartId);
        cartRepository.flush();
        cartOrders.sync(userId);
    }

    @Override
    @Transactional
    public void removeCartByUserId(Long userId) {
        entityManager.find(com.dodostore.dodostore.entity.UserEntity.class, userId, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        cartRepository.deleteByUserId(userId);
        cartRepository.flush();
        cartOrders.sync(userId);
    }

    @Override
    @Transactional
    public boolean replaceQuantityProduct(Long userId, Long cartId, Long delta) {
        if (delta == null || (delta != 1 && delta != -1)) return false;
        entityManager.find(com.dodostore.dodostore.entity.UserEntity.class, userId, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        CartEntity cart = cartRepository.findForQuantityUpdate(userId, cartId);
        return cart != null && setQuantity(userId, cartId, Math.addExact(cart.getQuantity(), delta));
    }

    @Override
    @Transactional
    public boolean setQuantity(Long userId, Long cartId, Long quantity) {
        if (quantity == null || quantity < 1) return false;
        entityManager.find(com.dodostore.dodostore.entity.UserEntity.class, userId, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        CartEntity cart = cartRepository.findForQuantityUpdate(userId, cartId);
        if (cart == null) return false;
        ProductEntity product = cart.getProduct();
        if (product.getStatus() != com.dodostore.dodostore.enums.StatusEnum.Active) return false;
        long others = 0, sameSize = 0;
        for (CartEntity other : cartRepository.findByUserId(userId)) {
            if (other.getId().equals(cartId) || !other.getProduct().getId().equals(product.getId())) continue;
            others = Math.addExact(others, other.getQuantity());
            if (java.util.Objects.equals(other.getSize(), cart.getSize())
                    && (!product.hasVariantStock() || java.util.Objects.equals(other.getColor(), cart.getColor()))) sameSize = Math.addExact(sameSize, other.getQuantity());
        }
        if (product.getQuantity() == null || quantity > product.getQuantity() - others
                || quantity > product.stockForVariant(cart.getColor(), cart.getSize()) - sameSize) return false;
        cart.setQuantity(quantity);
        cartRepository.saveAndFlush(cart);
        cartOrders.sync(userId);
        return true;
    }
}
