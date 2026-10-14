package com.dodostore.dodostore;

import com.dodostore.dodostore.config.VnpayProperties;
import com.dodostore.dodostore.entity.*;
import com.dodostore.dodostore.enums.StatusOrderEnum;
import com.dodostore.dodostore.repository.*;
import com.dodostore.dodostore.service.impl.VnpayService;
import com.dodostore.dodostore.utils.VnpaySignature;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VnpayServiceTest {
    PaymentTransactionRepository repo;
    OrderDetailRepository details;
    VnpayService service;
    PaymentTransaction payment;
    Map<String,String> params;
    @BeforeEach void setup() {
        VnpayProperties config = new VnpayProperties(); config.setTmnCode("TEST1234"); config.setHashSecret("test-secret");
        repo = mock(PaymentTransactionRepository.class); details = mock(OrderDetailRepository.class);
        service = new VnpayService(config, repo, mock(CartRepository.class), details);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "em", mock(jakarta.persistence.EntityManager.class));
        OrderEntity order = new OrderEntity(); order.setId(1L); order.setStatus(StatusOrderEnum.Cho_Thanh_Toan);
        payment = new PaymentTransaction(); payment.setOrder(order); payment.setAmount(10000L); payment.setTxnRef("abc");
        when(repo.findByTxnRef("abc")).thenReturn(Optional.of(payment));
        when(repo.lockByRef("abc")).thenReturn(Optional.of(payment));
        when(details.findByOrderId(1L)).thenReturn(new ArrayList<>());
        params = new HashMap<>(Map.of("vnp_TmnCode", "TEST1234", "vnp_TxnRef", "abc", "vnp_Amount", "1000000",
            "vnp_ResponseCode", "00", "vnp_TransactionStatus", "00"));
    }
    void sign() { params.put("vnp_SecureHash", VnpaySignature.sign(params, "test-secret")); }
    @Test void verifiesSignatureBeforeDatabaseAccess() {
        assertEquals("97", service.ipn(params).get("RspCode")); verifyNoInteractions(repo);
    }
    @Test void checksAmountBeforeMutation() {
        params.put("vnp_Amount", "100"); sign();
        assertEquals("04", service.ipn(params).get("RspCode")); assertEquals("PENDING", payment.getStatus());
    }
    @Test void repeatedSuccessDoesNotProcessDetailsAgain() {
        sign(); assertEquals("00", service.ipn(params).get("RspCode"));
        assertEquals("SUCCESS", payment.getStatus()); assertEquals(StatusOrderEnum.Dang_Giao, payment.getOrder().getStatus());
        assertEquals("02", service.ipn(params).get("RspCode")); verify(details, times(1)).findByOrderId(1L);
    }
    @Test void cancelIsTerminal() {
        params.put("vnp_ResponseCode", "24"); params.put("vnp_TransactionStatus", "02"); sign();
        assertEquals("00", service.ipn(params).get("RspCode")); assertEquals("CANCELLED", payment.getStatus());
        assertEquals(StatusOrderEnum.Da_Huy, payment.getOrder().getStatus());
    }
    @Test void missingOrder() {
        when(repo.lockByRef("abc")).thenReturn(Optional.empty()); sign();
        assertEquals("01", service.ipn(params).get("RspCode"));
    }
    @Test void stockSoldOnceOnSuccessAndNeverDowngraded() {
        ProductEntity product = new ProductEntity(); product.setId(7L); product.setQuantity(8L); product.setQuantitySell(1L);
        OrderDetailEntity line = new OrderDetailEntity(); line.setProduct(product); line.setQuantity(2L);
        when(details.findByOrderId(1L)).thenReturn(new ArrayList<>(List.of(line)));
        sign(); service.ipn(params); service.ipn(params);
        assertEquals(3L, product.getQuantitySell()); assertEquals(8L, product.getQuantity());
        params.put("vnp_ResponseCode", "24"); params.put("vnp_TransactionStatus", "02"); sign();
        assertEquals("02", service.ipn(params).get("RspCode")); assertEquals("SUCCESS", payment.getStatus());
        assertEquals(8L, product.getQuantity());
    }
    @Test void releasesReservationOnceOnFailure() {
        ProductEntity product = new ProductEntity(); product.setId(7L); product.setQuantity(8L); product.setQuantitySell(1L);
        OrderDetailEntity line = new OrderDetailEntity(); line.setProduct(product); line.setQuantity(2L);
        when(details.findByOrderId(1L)).thenReturn(new ArrayList<>(List.of(line)));
        params.put("vnp_ResponseCode", "24"); params.put("vnp_TransactionStatus", "02"); sign();
        service.ipn(params); service.ipn(params);
        assertEquals(10L, product.getQuantity()); assertEquals(1L, product.getQuantitySell());
    }
    @Test void returnSuccessBeforeIpnUpdatesOrderOnce() {
        sign();
        assertEquals("SUCCESS", service.result(params).get("status"));
        assertEquals(StatusOrderEnum.Dang_Giao, payment.getOrder().getStatus());
        assertEquals("02", service.ipn(params).get("RspCode"));
        service.result(params);
        verify(details, times(1)).findByOrderId(1L);
    }
    @Test void cancellationReturnWithoutTransactionStatus() {
        params.put("vnp_ResponseCode", "24"); params.remove("vnp_TransactionStatus"); sign();
        assertEquals("CANCELLED", service.result(params).get("status"));
        assertEquals(StatusOrderEnum.Da_Huy, payment.getOrder().getStatus());
        service.result(params);
        verify(details, times(1)).findByOrderId(1L);
    }
    @Test void invalidReturnCannotChangeOrder() {
        assertThrows(IllegalArgumentException.class, () -> service.result(params));
        assertEquals(StatusOrderEnum.Cho_Thanh_Toan, payment.getOrder().getStatus());
        verifyNoInteractions(repo);
    }
    @Test void intermediateReturnKeepsOrderPending() {
        params.put("vnp_TransactionStatus", "01"); sign();
        assertEquals("PENDING", service.result(params).get("status"));
        assertEquals(StatusOrderEnum.Cho_Thanh_Toan, payment.getOrder().getStatus());
        verifyNoInteractions(details);
    }
}
