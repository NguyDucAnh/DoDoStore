package com.dodostore.dodostore;
import com.dodostore.dodostore.dto.request.ProductRequest;
import com.dodostore.dodostore.entity.*;
import com.dodostore.dodostore.enums.StatusEnum;
import com.dodostore.dodostore.repository.*;
import com.dodostore.dodostore.mapper.ProductMapper;
import com.dodostore.dodostore.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ProductStockTotalTest {
 @Test void createComputesTotalFromSizesEvenIfClientSendsDifferentQuantity(){
  var products=mock(ProductRepository.class);var service=new ProductServiceImpl(products,org.mapstruct.factory.Mappers.getMapper(ProductMapper.class),mock(BrandRepository.class),mock(CategoryRepository.class),mock(OrderDetailRepository.class));
  when(products.save(any())).thenAnswer(i->i.getArgument(0));
  var request=ProductRequest.builder().name("Áo").price(100000L).discount(0L).color("Đen").sizes("S,M").sizeStock("S:02,M:003").quantity(999L).build();
  var result=service.createProduct(request);assertEquals(5L,result.getQuantity());assertEquals("S:2,M:3",result.getSizeStock());
 }
 @Test void updateIgnoresManualTotalAndRecomputesFromSizes(){
  var products=mock(ProductRepository.class);var service=new ProductServiceImpl(products,org.mapstruct.factory.Mappers.getMapper(ProductMapper.class),mock(BrandRepository.class),mock(CategoryRepository.class),mock(OrderDetailRepository.class));
  ReflectionTestUtils.setField(service,"em",mock(jakarta.persistence.EntityManager.class));
  var product=ProductEntity.builder().id(1L).name("Áo").price(100000L).discount(0L).sizes("S,M").sizeStock("S:2,M:3").quantity(5L).build();
  when(products.findByIdAndStatus(1L,StatusEnum.Active)).thenReturn(product);
  var request=ProductRequest.builder().sizeStock("S:4,M:6").quantity(-999L).build();
  var result=service.updateProduct(1L,request);assertEquals(10L,result.getQuantity());
 }
}
