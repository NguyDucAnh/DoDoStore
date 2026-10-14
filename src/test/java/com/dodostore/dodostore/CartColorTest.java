package com.dodostore.dodostore;

import com.dodostore.dodostore.entity.*;
import com.dodostore.dodostore.enums.StatusEnum;
import com.dodostore.dodostore.repository.*;
import com.dodostore.dodostore.service.impl.CartServiceImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartColorTest {
    CartServiceImpl service;
    CartRepository carts;
    ProductEntity product;
    List<CartEntity> lines;
    @BeforeEach void setup() {
        service = new CartServiceImpl();
        carts = mock(CartRepository.class);
        ReflectionTestUtils.setField(service, "cartOrders", mock(com.dodostore.dodostore.service.impl.CartOrderService.class));
        ProductRepository products = mock(ProductRepository.class);
        UserRepository users = mock(UserRepository.class);
        ReflectionTestUtils.setField(service, "cartRepository", carts);
        ReflectionTestUtils.setField(service, "productRepository", products);
        ReflectionTestUtils.setField(service, "userRepository", users);
        ReflectionTestUtils.setField(service, "entityManager", mock(EntityManager.class));
        product = ProductEntity.builder().id(7L).color("Đen,Trắng").sizes("M,L").sizeStock("M:3,L:1")
                .status(StatusEnum.Active).quantity(4L).build();
        lines = new ArrayList<>();
        when(products.findById(7L)).thenReturn(Optional.of(product));
        when(users.findById(1L)).thenReturn(Optional.of(new UserEntity()));
        when(carts.findByUserId(1L)).thenAnswer(i -> lines);
        when(carts.findByUserIdAndProductIdAndSizeAndColor(anyLong(), anyLong(), anyString(), anyString()))
            .thenAnswer(i -> lines.stream().filter(c -> c.getSize().equals(i.getArgument(2))
                    && c.getColor().equals(i.getArgument(3))).findFirst().orElse(null));
        when(carts.saveAndFlush(any(CartEntity.class))).thenAnswer(i -> {
            CartEntity c = i.getArgument(0);
            if (lines.stream().noneMatch(existing -> existing == c)) lines.add(c);
            return c;
        });
    }
    @Test void separatesColorsAndMergesOnlyMatchingPair() {
        assertTrue(service.addCart(1L, 7L, "M", "Đen"));
        assertTrue(service.addCart(1L, 7L, "M", "Trắng"));
        assertTrue(service.addCart(1L, 7L, "M", "đen"));
        assertEquals(2, lines.size());
        assertEquals(2L, lines.get(0).getQuantity());
        assertEquals(1L, lines.get(1).getQuantity());
    }
    @Test void rejectsInvalidColorAndRespectsSharedStock() {
        assertFalse(service.addCart(1L, 7L, "M", "Đỏ"));
        assertFalse(service.addCart(1L, 7L, "M", null));
        product.setQuantity(1L); product.setSizeStock("M:1,L:0");
        assertTrue(service.addCart(1L, 7L, "M", "Đen"));
        assertFalse(service.addCart(1L, 7L, "L", "Trắng"));
        assertEquals(1, lines.size());
    }

    @Test void independentColorLimitsApplyToAddAndTypedQuantity() {
        product.setVariantStock("{\"Đen\":{\"M\":1,\"L\":0},\"Trắng\":{\"M\":2,\"L\":1}}");
        assertTrue(service.addCart(1L,7L,"M","Đen"));
        assertFalse(service.addCart(1L,7L,"M","Đen"));
        assertTrue(service.addCart(1L,7L,"M","Trắng"));
        lines.get(0).setId(1L);lines.get(1).setId(2L);
        when(carts.findForQuantityUpdate(1L,2L)).thenReturn(lines.get(1));
        assertTrue(service.setQuantity(1L,2L,2L));
        assertFalse(service.setQuantity(1L,2L,3L));
        assertEquals(1L,lines.get(0).getQuantity());
    }
}
