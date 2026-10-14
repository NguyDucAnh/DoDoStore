package com.dodostore.dodostore;
import com.dodostore.dodostore.entity.ProductEntity;
import com.dodostore.dodostore.utils.VariantStock;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class VariantStockTest {
 private static final String STOCK = "{\"Đen\":{\"S\":2,\"M\":0},\"Trắng\":{\"S\":5,\"M\":3}}";
 @Test void validateAndComputeTotals() {
  assertEquals(10,VariantStock.total(VariantStock.validate("Đen,Trắng","S,M",STOCK)));
  assertEquals(0,VariantStock.available(STOCK,"Đen","M"));
  assertEquals(3,VariantStock.available(STOCK,"Trắng","M"));
 }
 @Test void buyingAndRestoringAColorDoesNotChangeAnotherColor() {
  var p=ProductEntity.builder().name("Áo").quantity(10L).variantStock(STOCK).sizeStock("S:7,M:3").build();
  p.reserveVariant("Trắng","S",4);
  assertEquals(1,p.stockForVariant("Trắng","S"));assertEquals(2,p.stockForVariant("Đen","S"));assertEquals(6,p.getQuantity());
  assertThrows(IllegalArgumentException.class,()->p.reserveVariant("Đen","M",1));
  assertThrows(IllegalArgumentException.class,()->p.reserveVariant("Trắng","S",2));
  p.releaseVariant("Trắng","S",4);assertEquals(10,p.getQuantity());assertEquals(5,p.stockForVariant("Trắng","S"));
 }
 @Test void rejectNegativeFractionAndMissingSize() {
  assertThrows(IllegalArgumentException.class,()->VariantStock.parse("{\"Đen\":{\"S\":-1}}"));
  assertThrows(IllegalArgumentException.class,()->VariantStock.parse("{\"Đen\":{\"S\":1.5}}"));
  assertThrows(IllegalArgumentException.class,()->VariantStock.validate("Đen,Trắng","S,M,L",STOCK));
 }
 @Test void legacyStockRemainsSharedUntilAllocated() {
  var p=ProductEntity.builder().quantity(7L).sizeStock("S:7").build();
  p.reserveVariant("Đen","S",2);
  assertEquals(5,p.stockForVariant("Trắng","S"));
 }
}
