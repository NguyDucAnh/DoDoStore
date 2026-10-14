package com.dodostore.dodostore;

import com.dodostore.dodostore.entity.*;
import com.dodostore.dodostore.enums.*;
import com.dodostore.dodostore.repository.*;
import com.dodostore.dodostore.service.impl.*;
import jakarta.persistence.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CodCheckoutStatusTest {
    @Test void codSubmitsSameDraftAndMovesToShipping() {
        UserServiceImpl service = new UserServiceImpl();
        EntityManager em = mock(EntityManager.class);
        AddressRepository addresses = mock(AddressRepository.class);
        CartRepository carts = mock(CartRepository.class);
        OrderRepository orders = mock(OrderRepository.class);
        CartOrderService drafts = mock(CartOrderService.class);
        ReflectionTestUtils.setField(service, "entityManager", em);
        ReflectionTestUtils.setField(service, "addressRepository", addresses);
        ReflectionTestUtils.setField(service, "cartRepository", carts);
        ReflectionTestUtils.setField(service, "orderRepository", orders);
        ReflectionTestUtils.setField(service, "cartOrders", drafts);
        ReflectionTestUtils.setField(service, "orderDetailRepository", mock(OrderDetailRepository.class));
        ReflectionTestUtils.setField(service, "productRepository", mock(ProductRepository.class));
        UserEntity user = new UserEntity(); user.setId(1L); user.setStatus(StatusEnum.Active);
        AddressEntity address = new AddressEntity(); address.setUser(user);
        address.setAddress("Hà Nội"); address.setPhone("0900000000");
        ProductEntity product = ProductEntity.builder().id(7L).price(100000L).discount(0L)
                .status(StatusEnum.Active).sizes("M,L").sizeStock("M:5,L:5").color("Đen").quantity(10L).quantitySell(0L).build();
        CartEntity cart = CartEntity.builder().user(user).product(product).quantity(2L).size("M").color("Đen").build();
        OrderEntity draft = new OrderEntity(); draft.setId(42L); draft.setCartDraft(false);
        when(em.find(UserEntity.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(user);
        when(addresses.findById(2L)).thenReturn(Optional.of(address));
        when(carts.findByUserId(1L)).thenReturn(List.of(cart));
        when(drafts.submit(user)).thenReturn(draft);
        service.checkout(1L, 2L);
        assertEquals(42L, draft.getId()); assertEquals(StatusOrderEnum.Dang_Giao, draft.getStatus());
        assertEquals(200000L, draft.getTotal()); assertEquals(8L, product.getQuantity());
        assertEquals(3L, product.stockForSize("M")); assertEquals(5L, product.stockForSize("L"));
        assertEquals(2L, product.getQuantitySell()); verify(carts).deleteByUserId(1L);
    }
}
