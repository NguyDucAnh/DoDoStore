package com.dodostore.dodostore;
import com.dodostore.dodostore.entity.*;
import com.dodostore.dodostore.enums.*;
import com.dodostore.dodostore.repository.*;
import com.dodostore.dodostore.service.impl.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class CustomerImprovementsTest {
 @Test void higherRegularDiscountSuppressesFlashSaleAndPreservesPrice() {
  var p=ProductEntity.builder().discount(40L).flashSaleDiscount(20L).flashSaleStart(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).minusDays(1)).flashSaleEnd(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(1)).build();
  assertFalse(p.isFlashSaleActive());assertEquals(40L,p.getEffectiveDiscount());
  p.setDiscount(20L);assertFalse(p.isFlashSaleActive());
  p.setDiscount(10L);assertTrue(p.isFlashSaleActive());assertEquals(20L,p.getEffectiveDiscount());
 }
 @Test void orderDetailsRequireOwnerAndRejectDrafts() {
  var orders=mock(OrderRepository.class);var lines=mock(OrderDetailRepository.class);var service=new CustomerOrderDetailService(orders,lines);
  var order=new OrderEntity();order.setId(7L);order.setUser(UserEntity.builder().id(1L).build());order.setCartDraft(false);
  when(orders.findById(7L)).thenReturn(Optional.of(order));
  assertThrows(ResponseStatusException.class,()->service.view(2L,7L));assertThrows(ResponseStatusException.class,()->service.view(null,7L));verifyNoInteractions(lines);
  order.setCartDraft(true);assertThrows(ResponseStatusException.class,()->service.view(1L,7L));verifyNoInteractions(lines);
 }
 @Test void detailsUsePurchasedValuesAndVietnamTime() {
  var orders=mock(OrderRepository.class);var lines=mock(OrderDetailRepository.class);var service=new CustomerOrderDetailService(orders,lines);
  var order=new OrderEntity();order.setId(7L);order.setUser(UserEntity.builder().id(1L).build());order.setCartDraft(false);order.setDate(Date.from(Instant.parse("2026-09-22T09:30:00Z")));order.setStatus(StatusOrderEnum.Dang_Giao);order.setTotal(120000L);
  var line=OrderDetailEntity.builder().product(ProductEntity.builder().id(3L).name("Áo polo").price(999999L).build()).color("Đen").size("M").quantity(2L).total(120000L).build();
  when(orders.findById(7L)).thenReturn(Optional.of(order));when(lines.findByOrderId(7L)).thenReturn(List.of(line));
  var result=service.view(1L,7L);assertEquals("16:30 22/09/2026",result.placedAt());assertEquals("Đang giao",result.status());assertEquals(120000L,result.items().get(0).total());assertEquals("Đen",result.items().get(0).color());assertEquals(2L,result.items().get(0).quantity());
 }
 @Test void missingOrderReturnsNotFound() {
  var orders=mock(OrderRepository.class);when(orders.findById(99L)).thenReturn(Optional.empty());
  assertThrows(ResponseStatusException.class,()->new CustomerOrderDetailService(orders,mock(OrderDetailRepository.class)).view(1L,99L));
 }
 @Test void allOrderStatusesHaveVietnameseLabels() {
  assertEquals(List.of("Đang xử lý","Đang giao","Đã giao","Chờ thanh toán","Đã hủy"),Arrays.stream(StatusOrderEnum.values()).map(StatusOrderEnum::getDisplayName).toList());
 }
 @Test void chatCardsResolveOnlyExistingActiveProductsAndDeduplicate() {
  var repo=mock(ProductRepository.class);var service=new ChatProductCardService(repo);
  var p=ProductEntity.builder().id(1L).name("Áo polo").price(100000L).discount(20L).status(StatusEnum.Active).build();
  var closed=ProductEntity.builder().id(2L).status(StatusEnum.Closed).price(10L).build();
  when(repo.findAllById(any())).thenReturn(List.of(p,closed));
  var cards=service.fromReply("[Áo](/home/product/1) /home/product/1 /home/product/2 /home/product/999");
  assertEquals(1,cards.size());assertEquals("/home/product/1",cards.get(0).url());assertEquals(80000L,cards.get(0).salePrice());
 }
 @Test void chatWithoutProductLinksDoesNotQueryCatalog() {
  var repo=mock(ProductRepository.class);assertTrue(new ChatProductCardService(repo).fromReply("Xin chào").isEmpty());verifyNoInteractions(repo);
 }
}
