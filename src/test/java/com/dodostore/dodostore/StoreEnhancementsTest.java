package com.dodostore.dodostore;

import com.dodostore.dodostore.entity.*;
import com.dodostore.dodostore.enums.StatusEnum;
import com.dodostore.dodostore.repository.*;
import com.dodostore.dodostore.service.impl.*;
import com.dodostore.dodostore.utils.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreEnhancementsTest {
    private ProductEntity product() {
        return ProductEntity.builder().id(1L).name("Áo").sizes("S,M").sizeStock("S:3,M:7")
                .quantity(10L).price(100000L).discount(10L).status(StatusEnum.Active).build();
    }
    @Test void subtractsOnlyPurchasedSizeAndRestoresSameSize() {
        ProductEntity p = product();
        p.reserveSize("S", 2);
        assertEquals(1, p.stockForSize("S")); assertEquals(7, p.stockForSize("M"));
        assertEquals(8L, p.getQuantity());
        p.releaseSize("S", 2);
        assertEquals(3, p.stockForSize("S")); assertEquals(10L, p.getQuantity());
    }
    @Test void refusesOversellingEvenWhenTotalStockIsSufficient() {
        ProductEntity p = product();
        assertThrows(IllegalArgumentException.class, () -> p.reserveSize("S",4));
        assertEquals(10L,p.getQuantity()); assertEquals(3,p.stockForSize("S"));
    }
    @Test void refusesNonPositivePurchases() {
        assertThrows(IllegalArgumentException.class, () -> product().reserveSize("S",-1));
        assertThrows(IllegalArgumentException.class, () -> product().reserveSize("S",0));
    }
    @Test void legacyStockRequiresExplicitAllocation() {
        ProductEntity p=product();p.setSizeStock(null);
        assertEquals(0,p.stockForSize("S"));
        assertThrows(IllegalArgumentException.class, () -> p.reserveSize("S",1));
    }
    @Test void stockValidationRejectsNegativeDuplicateAndMissingSizes() {
        assertThrows(IllegalArgumentException.class, () -> SizeStock.validate("S,M","S:-1,M:2"));
        assertThrows(IllegalArgumentException.class, () -> SizeStock.validate("S,M","S:1,S:2,M:2"));
        assertThrows(IllegalArgumentException.class, () -> SizeStock.validate("S,M","S:1"));
        assertThrows(IllegalArgumentException.class, () -> SizeStock.validate("S,M","S:1,M:2,L:3"));
        assertThrows(IllegalArgumentException.class, () -> SizeStock.validate("S,M","S:1.5,M:2"));
        assertEquals(5,SizeStock.total(SizeStock.validate("S,M","s:2,m:3")));
    }
    @Test void flashSaleIncludesStartExcludesEnd() {
        LocalDateTime start=LocalDateTime.of(2026,9,20,12,0),end=start.plusHours(1);
        assertFalse(FlashSale.active(30L,start,end,start.minusNanos(1)));
        assertTrue(FlashSale.active(30L,start,end,start));
        assertFalse(FlashSale.active(30L,start,end,end));
        assertFalse(FlashSale.active(0L,start,end,start));
    }
    @Test void flashSaleValidatesRangeAndSchedule() {
        LocalDateTime start=LocalDateTime.of(2026,9,20,12,0);
        assertThrows(IllegalArgumentException.class, () -> FlashSale.validate(30L,start,start));
        assertThrows(IllegalArgumentException.class, () -> FlashSale.validate(100L,start,start.plusHours(1)));
        assertThrows(IllegalArgumentException.class, () -> FlashSale.validate(-1L,start,start.plusHours(1)));
        assertThrows(IllegalArgumentException.class, () -> FlashSale.validate(20L,null,null));
    }
    @Test void effectiveDiscountDoesNotStackAndExpires() {
        ProductEntity p=product();
        LocalDateTime now=LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        p.setFlashSaleStart(now.minusHours(1));p.setFlashSaleEnd(now.plusHours(1));p.setFlashSaleDiscount(30L);
        assertEquals(30L,p.getEffectiveDiscount());assertEquals(10L,p.getDiscount());
        p.setFlashSaleDiscount(5L);assertEquals(10L,p.getEffectiveDiscount());
        p.setFlashSaleDiscount(30L);p.setFlashSaleEnd(now.minusMinutes(1));assertEquals(10L,p.getEffectiveDiscount());
    }
    @Test void cartChecksAggregateOfSameSizeAcrossColors() {
        ProductEntity p=product();
        CartEntity first=CartEntity.builder().id(10L).product(p).size("S").color("Đen").quantity(1L).build();
        CartEntity second=CartEntity.builder().id(11L).product(p).size("S").color("Trắng").quantity(2L).build();
        CartEntity otherSize=CartEntity.builder().id(12L).product(p).size("M").color("Đen").quantity(1L).build();
        CartRepository carts=mock(CartRepository.class);
        when(carts.findForQuantityUpdate(1L,10L)).thenReturn(first);
        when(carts.findByUserId(1L)).thenReturn(List.of(first,second,otherSize));
        CartServiceImpl service=new CartServiceImpl();
        ReflectionTestUtils.setField(service,"cartRepository",carts);
        ReflectionTestUtils.setField(service,"entityManager",mock(EntityManager.class));
        ReflectionTestUtils.setField(service,"cartOrders",mock(CartOrderService.class));
        assertFalse(service.setQuantity(1L,10L,2L));
        assertFalse(service.setQuantity(1L,10L,-1L));
        assertFalse(service.setQuantity(1L,10L,0L));
        assertFalse(service.setQuantity(1L,10L,Long.MAX_VALUE));
        assertTrue(service.setQuantity(1L,10L,1L));
        verify(carts,times(1)).saveAndFlush(first);
    }
    @Test void cartDoesNotUpdateAnotherUsersLine() {
        CartServiceImpl service=new CartServiceImpl();
        ReflectionTestUtils.setField(service,"cartRepository",mock(CartRepository.class));
        ReflectionTestUtils.setField(service,"entityManager",mock(EntityManager.class));
        assertFalse(service.setQuantity(999L,10L,1L));
    }
    @Test void revenueIncludesZeroDays() {
        OrderRepository repo=mock(OrderRepository.class);
        java.time.LocalDate start=java.time.LocalDate.of(2026,9,1);
        when(repo.findRevenueByDay(any(),any())).thenReturn(java.util.Collections.singletonList(new Object[]{java.sql.Date.valueOf(start.plusDays(1)),new java.math.BigDecimal("150000")}));
        OrderServiceImpl service=new OrderServiceImpl();ReflectionTestUtils.setField(service,"orderRepository",repo);
        var days=service.getRevenueByDay(start,start.plusDays(2));
        assertEquals(3,days.size());assertEquals(java.math.BigDecimal.ZERO,days.get(0).getRevenue());
        assertEquals(new java.math.BigDecimal("150000"),days.get(1).getRevenue());
        assertEquals(java.math.BigDecimal.ZERO,days.get(2).getRevenue());
    }
    @Test void vnpayCancellationRestoresStockOnlyOnce() {
        ProductEntity product=product();product.reserveSize("S",2);
        OrderEntity order=new OrderEntity();order.setId(5L);
        PaymentTransaction payment=new PaymentTransaction();payment.setOrder(order);payment.setAmount(180000L);
        PaymentTransactionRepository payments=mock(PaymentTransactionRepository.class);
        when(payments.lockByRef("ref")).thenReturn(java.util.Optional.of(payment));
        OrderDetailEntity detail=new OrderDetailEntity();detail.setProduct(product);detail.setSize("S");detail.setQuantity(2L);
        OrderDetailRepository details=mock(OrderDetailRepository.class);
        when(details.findByOrderId(5L)).thenReturn(new java.util.ArrayList<>(List.of(detail)));
        VnpayService service=spy(new VnpayService(new com.dodostore.dodostore.config.VnpayProperties(),payments,mock(CartRepository.class),details));
        ReflectionTestUtils.setField(service,"em",mock(EntityManager.class));
        doReturn(true).when(service).authentic(anyMap());
        var callback=java.util.Map.of("vnp_TxnRef","ref","vnp_Amount","18000000","vnp_ResponseCode","24","vnp_TransactionStatus","02");
        assertEquals("00",service.ipn(callback).get("RspCode"));
        assertEquals(3,product.stockForSize("S"));assertEquals(10L,product.getQuantity());
        assertEquals("02",service.ipn(callback).get("RspCode"));
        assertEquals(3,product.stockForSize("S"));assertEquals(10L,product.getQuantity());
    }
    @Test void responseMapsSalePriceAndKeepsBaseDiscountForAdmin() {
        ProductEntity p=product();LocalDateTime now=LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        p.setFlashSaleStart(now.minusHours(1));p.setFlashSaleEnd(now.plusHours(1));p.setFlashSaleDiscount(40L);
        var mapper=org.mapstruct.factory.Mappers.getMapper(com.dodostore.dodostore.mapper.ProductMapper.class);
        var response=mapper.toProductResponse(p);
        assertEquals(40L,response.getDiscount());assertEquals(10L,response.getBaseDiscount());
        assertTrue(response.isFlashSaleActive());assertEquals(3,response.stockForSize("S"));
    }
}
