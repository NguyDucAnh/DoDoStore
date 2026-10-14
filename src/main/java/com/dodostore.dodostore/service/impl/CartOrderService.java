package com.dodostore.dodostore.service.impl;

import com.dodostore.dodostore.entity.*;
import com.dodostore.dodostore.enums.StatusOrderEnum;
import com.dodostore.dodostore.repository.CartRepository;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/** One draft per cart. Callers lock the owner before changing cart or checking out. */
@Service
@Transactional
public class CartOrderService {
    @PersistenceContext private EntityManager em;
    @Autowired private CartRepository carts;

    private OrderEntity draft(UserEntity user) {
        List<OrderEntity> found = em.createQuery(
                "select o from OrderEntity o where o.user.id = :userId and o.cartDraft = true", OrderEntity.class)
                .setParameter("userId", user.getId()).setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();
        if (!found.isEmpty()) return found.get(0);
        OrderEntity order = new OrderEntity();
        order.setUser(user); order.setDate(new Date());
        order.setCartDraft(true); order.setStatus(StatusOrderEnum.Dang_Xu_Ly);
        em.persist(order);
        return order;
    }

    private void clearLines(OrderEntity order) {
        for (OrderDetailEntity line : em.createQuery(
                "select d from OrderDetailEntity d where d.order.id = :id", OrderDetailEntity.class)
                .setParameter("id", order.getId()).getResultList()) em.remove(line);
        em.flush();
        order.setItems(new ArrayList<>());
    }

    public void sync(Long userId) {
        UserEntity user = em.find(UserEntity.class, userId, LockModeType.PESSIMISTIC_WRITE);
        if (user == null) throw new IllegalArgumentException("Không tìm thấy tài khoản.");
        List<CartEntity> items = carts.findByUserId(userId);
        OrderEntity order = draft(user);
        clearLines(order);
        if (items.isEmpty()) { em.remove(order); return; }
        long total = 0;
        for (CartEntity cart : items) {
            ProductEntity product = cart.getProduct();
            long discount = product.getEffectiveDiscount() == null ? 0 : product.getEffectiveDiscount();
            long price = product.getPrice() == null ? 0 : product.getPrice();
            long amount = Math.multiplyExact(Math.multiplyExact(price, 100 - discount) / 100, cart.getQuantity());
            OrderDetailEntity line = new OrderDetailEntity();
            line.setOrder(order); line.setProduct(product); line.setQuantity(cart.getQuantity());
            line.setSize(cart.getSize()); line.setColor(cart.getColor());
            line.setPrice(price); line.setDiscount(discount); line.setTotal(amount);
            em.persist(line); order.getItems().add(line);
            total = Math.addExact(total, amount);
        }
        order.setTotal(total); order.setQuantity((long) items.size());
        order.setStatus(StatusOrderEnum.Dang_Xu_Ly);
        // Drafts do not reserve stock or count as sales.
    }

    public OrderEntity submit(UserEntity user) {
        OrderEntity order = draft(user);
        clearLines(order); // Checkout recreates snapshots after validating price and stock.
        order.setCartDraft(false); order.setDate(new Date());
        return order;
    }
}
