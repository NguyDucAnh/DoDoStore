package com.dodostore.dodostore;
import com.dodostore.dodostore.entity.OrderEntity;
import com.dodostore.dodostore.mapper.OrderMapper;
import com.dodostore.dodostore.repository.OrderRepository;
import com.dodostore.dodostore.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.mockito.Mockito.*;
class OrderNewestFirstTest {
 @Test void bothListsUseNewestDateThenDescendingIdWithNullDatesLast() {
  var service=new OrderServiceImpl();var repo=mock(OrderRepository.class);var mapper=mock(OrderMapper.class);
  ReflectionTestUtils.setField(service,"orderRepository",repo);ReflectionTestUtils.setField(service,"orderMapper",mapper);
  var old=new OrderEntity();old.setId(9L);old.setDate(new Date(1000));
  var first=new OrderEntity();first.setId(2L);first.setDate(new Date(2000));
  var second=new OrderEntity();second.setId(3L);second.setDate(new Date(2000));
  var missing=new OrderEntity();missing.setId(10L);
  var list=List.of(old,first,missing,second);
  when(repo.findAll()).thenReturn(list);when(repo.findByUserId(1L)).thenReturn(list);
  service.getAllOrder();service.historyBuy(1L);
  var sequence=inOrder(mapper);
  for(int i=0;i<2;i++) {sequence.verify(mapper).toOrderResponse(second);sequence.verify(mapper).toOrderResponse(first);sequence.verify(mapper).toOrderResponse(old);sequence.verify(mapper).toOrderResponse(missing);}
 }
}
