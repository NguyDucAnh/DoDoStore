package com.dodostore.dodostore;

import com.dodostore.dodostore.entity.*;
import com.dodostore.dodostore.enums.StatusOrderEnum;
import com.dodostore.dodostore.repository.CartRepository;
import com.dodostore.dodostore.service.impl.CartOrderService;
import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartOrderServiceTest {
    CartOrderService service;
    EntityManager em;
    CartRepository carts;
    UserEntity user;
    OrderEntity order;
    ProductEntity product;
    CartEntity cart;
    TypedQuery<OrderEntity> draftQuery;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() {
        service = new CartOrderService(); em = mock(EntityManager.class); carts = mock(CartRepository.class);
        ReflectionTestUtils.setField(service, "em", em);
        ReflectionTestUtils.setField(service, "carts", carts);
        user = new UserEntity(); user.setId(1L);
        order = new OrderEntity(); order.setId(10L); order.setUser(user);
        order.setCartDraft(true); order.setStatus(StatusOrderEnum.Dang_Xu_Ly);
        product = ProductEntity.builder().id(7L).price(100000L).discount(10L)
                .quantity(10L).quantitySell(2L).build();
        cart = CartEntity.builder().user(user).product(product).quantity(2L).size("M").color("Đen").build();
        draftQuery = mock(TypedQuery.class, RETURNS_SELF);
        TypedQuery<OrderDetailEntity> linesQuery = mock(TypedQuery.class, RETURNS_SELF);
        when(em.find(UserEntity.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(user);
        when(em.createQuery(anyString(), eq(OrderEntity.class))).thenReturn(draftQuery);
        when(draftQuery.getResultList()).thenReturn(List.of(order));
        when(em.createQuery(anyString(), eq(OrderDetailEntity.class))).thenReturn(linesQuery);
        when(linesQuery.getResultList()).thenReturn(List.of());
        when(carts.findByUserId(1L)).thenReturn(List.of(cart));
    }

    @Test void cartChangesUpdateSameDraftWithoutReservingStock() {
        service.sync(1L);
        assertEquals(180000L, order.getTotal());
        assertEquals(StatusOrderEnum.Dang_Xu_Ly, order.getStatus());
        assertEquals(10L, product.getQuantity()); assertEquals(2L, product.getQuantitySell());
        cart.setQuantity(3L); service.sync(1L);
        assertEquals(270000L, order.getTotal()); assertEquals(10L, order.getId());
        assertEquals(1, order.getItems().size());
        verify(em, never()).persist(isA(OrderEntity.class));
    }

    @Test void checkoutReusesDraftAndClearsDraftMarker() {
        OrderEntity submitted = service.submit(user);
        assertSame(order, submitted); assertFalse(submitted.getCartDraft());
        verify(em, never()).persist(isA(OrderEntity.class));
    }

    @Test void emptyCartRemovesOnlyDraft() {
        when(carts.findByUserId(1L)).thenReturn(List.of());
        service.sync(1L); verify(em).remove(order);
        assertEquals(10L, product.getQuantity());
    }

    @Test void firstAdditionCreatesDraft() {
        when(draftQuery.getResultList()).thenReturn(List.of());
        service.sync(1L);
        var capture = org.mockito.ArgumentCaptor.forClass(OrderEntity.class);
        verify(em).persist(capture.capture());
        assertTrue(capture.getValue().getCartDraft());
        assertEquals(StatusOrderEnum.Dang_Xu_Ly, capture.getValue().getStatus());
        assertEquals(180000L, capture.getValue().getTotal());
    }
}
