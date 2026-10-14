package com.dodostore.dodostore;

import com.dodostore.dodostore.entity.*;
import com.dodostore.dodostore.enums.StatusOrderEnum;
import com.dodostore.dodostore.repository.OrderRepository;
import com.dodostore.dodostore.service.impl.OrderServiceImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderReceivedTest {
    OrderServiceImpl service;
    OrderRepository orders;
    OrderEntity order;

    @BeforeEach void setup() {
        service = new OrderServiceImpl(); orders = mock(OrderRepository.class);
        ReflectionTestUtils.setField(service, "orderRepository", orders);
        ReflectionTestUtils.setField(service, "entityManager", mock(EntityManager.class));
        UserEntity user = new UserEntity(); user.setId(1L);
        order = new OrderEntity(); order.setId(10L); order.setUser(user);
        order.setStatus(StatusOrderEnum.Dang_Giao);
        when(orders.findById(10L)).thenReturn(Optional.of(order));
    }

    @Test void ownerCanConfirmOnce() {
        service.confirmReceived(1L, 10L);
        service.confirmReceived(1L, 10L);
        assertEquals(StatusOrderEnum.Da_Giao, order.getStatus());
        verify(orders, times(1)).save(order);
    }

    @Test void anotherCustomerCannotConfirm() {
        assertThrows(IllegalArgumentException.class, () -> service.confirmReceived(2L, 10L));
        assertEquals(StatusOrderEnum.Dang_Giao, order.getStatus());
        verify(orders, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = StatusOrderEnum.class, names = {"Dang_Xu_Ly", "Cho_Thanh_Toan", "Da_Huy"})
    void blocksOrdersThatAreNotShipping(StatusOrderEnum status) {
        order.setStatus(status);
        assertThrows(IllegalStateException.class, () -> service.confirmReceived(1L, 10L));
        assertEquals(status, order.getStatus()); verify(orders, never()).save(any());
    }

    @Test void blocksDraftEvenWithWrongShippingStatus() {
        order.setCartDraft(true);
        assertThrows(IllegalStateException.class, () -> service.confirmReceived(1L, 10L));
        verify(orders, never()).save(any());
    }

    @Test void missingOrderHasFriendlyError() {
        when(orders.findById(10L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.confirmReceived(1L, 10L));
    }

    @Test void unauthenticatedCannotConfirm() {
        assertThrows(IllegalArgumentException.class, () -> service.confirmReceived(null, 10L));
        verifyNoInteractions(orders);
    }
}
