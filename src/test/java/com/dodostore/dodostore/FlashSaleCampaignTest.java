package com.dodostore.dodostore;
import com.dodostore.dodostore.dto.request.FlashSaleCampaignRequest;
import com.dodostore.dodostore.entity.*;
import com.dodostore.dodostore.enums.*;
import com.dodostore.dodostore.repository.*;
import com.dodostore.dodostore.service.impl.FlashSaleCampaignService;
import org.junit.jupiter.api.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class FlashSaleCampaignTest {
 FlashSaleCampaignRepository campaigns;ProductRepository products;FlashSaleCampaignService service;
 ProductEntity first,second,closed;FlashSaleCampaignRequest request;
 LocalDateTime now=LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).withSecond(0).withNano(0);
 @BeforeEach void setup(){
  campaigns=mock(FlashSaleCampaignRepository.class);products=mock(ProductRepository.class);service=new FlashSaleCampaignService(campaigns,products,mock(OrderDetailRepository.class));
  first=product(1L,StatusEnum.Active);second=product(2L,StatusEnum.Active);closed=product(3L,StatusEnum.Closed);
  request=new FlashSaleCampaignRequest();request.setName("Cuối tuần");request.setDiscount(30L);request.setStartsAt(now.minusHours(1));request.setEndsAt(now.plusHours(2));request.setProductIds(List.of(1L));
  when(products.lockCampaignCandidates(eq(StatusEnum.Active),nullable(Long.class))).thenReturn(List.of(first,second,closed));
  when(campaigns.save(any())).thenAnswer(inv->{FlashSaleCampaign c=inv.getArgument(0);if(c.getId()==null)c.setId(10L);return c;});
 }
 ProductEntity product(Long id,StatusEnum status){return ProductEntity.builder().id(id).name("Áo "+id).status(status).quantity(10L).sizeStock("M:10").sizes("M").discount(10L).price(100000L).build();}
 FlashSaleCampaign existing(){FlashSaleCampaign c=new FlashSaleCampaign();c.setId(5L);c.setVersion(1L);c.setName("Cũ");c.setScope(FlashSaleScope.SELECTED);c.setDiscount(20L);c.setStartsAt(now.minusDays(1));c.setEndsAt(now.plusDays(1));when(campaigns.lockById(5L)).thenReturn(Optional.of(c));return c;}
 @Test void selectedScopeChangesOnlySelectedProduct(){assertEquals(1,service.save(request));assertEquals(30L,first.getFlashSaleDiscount());assertEquals(30L,first.getEffectiveDiscount());assertNotNull(first.getFlashSaleCampaign());assertNull(second.getFlashSaleDiscount());assertNull(closed.getFlashSaleDiscount());assertEquals(10L,first.getQuantity());}
 @Test void allScopeChangesEveryActiveProductOnly(){request.setScope(FlashSaleScope.ALL);request.setProductIds(List.of());assertEquals(2,service.save(request));assertEquals(30L,first.getEffectiveDiscount());assertEquals(30L,second.getEffectiveDiscount());assertNull(closed.getFlashSaleDiscount());assertSame(first.getFlashSaleCampaign(),second.getFlashSaleCampaign());}
 @Test void noSelectionDoesNotSaveAnything(){request.setProductIds(List.of());assertThrows(IllegalArgumentException.class,()->service.save(request));verify(campaigns,never()).save(any());verify(products,never()).saveAll(any());assertNull(first.getFlashSaleDiscount());}
 @Test void unknownOrClosedSelectionIsRejectedAtomically(){request.setProductIds(List.of(1L,3L));assertThrows(IllegalArgumentException.class,()->service.save(request));assertNull(first.getFlashSaleDiscount());request.setProductIds(List.of(1L,999L));assertThrows(IllegalArgumentException.class,()->service.save(request));assertNull(first.getFlashSaleCampaign());}
 @Test void rejectsInvalidScheduleAndDiscount(){request.setEndsAt(request.getStartsAt());assertThrows(IllegalArgumentException.class,()->service.save(request));request.setEndsAt(now.minusMinutes(1));assertThrows(IllegalArgumentException.class,()->service.save(request));request.setEndsAt(now.plusHours(1));request.setDiscount(100L);assertThrows(IllegalArgumentException.class,()->service.save(request));request.setDiscount(0L);assertThrows(IllegalArgumentException.class,()->service.save(request));verify(campaigns,never()).save(any());}
 @Test void futureCampaignDoesNotDiscountBeforeItsStart(){request.setStartsAt(now.plusHours(1));service.save(request);assertFalse(first.isFlashSaleActive());assertEquals(10L,first.getEffectiveDiscount());}
 @Test void editingScopeRemovesDeselectedProducts(){FlashSaleCampaign c=existing();first.setFlashSaleCampaign(c);first.setFlashSaleDiscount(20L);second.setFlashSaleCampaign(c);second.setFlashSaleDiscount(20L);request.setId(5L);request.setVersion(1L);request.setProductIds(List.of(2L));assertEquals(1,service.save(request));assertNull(first.getFlashSaleCampaign());assertEquals(0L,first.getFlashSaleDiscount());assertSame(c,second.getFlashSaleCampaign());assertEquals(30L,second.getFlashSaleDiscount());}
 @Test void oldCampaignStopDoesNotClearNewCampaign(){FlashSaleCampaign old=existing();first.setFlashSaleCampaign(old);second.setFlashSaleCampaign(old);request.setProductIds(List.of(1L));service.save(request);when(products.lockByCampaign(5L)).thenReturn(List.of(second));service.stop(5L);assertEquals(30L,first.getFlashSaleDiscount());assertEquals(10L,first.getFlashSaleCampaign().getId());assertSame(old,second.getFlashSaleCampaign());assertEquals(0L,second.getFlashSaleDiscount());assertFalse(old.isEnabled());service.stop(5L);verify(products,times(1)).lockByCampaign(5L);}
 @Test void staleEditIsRejectedBeforeChangingProducts(){existing();request.setId(5L);request.setVersion(0L);assertThrows(IllegalArgumentException.class,()->service.save(request));verify(products,never()).lockCampaignCandidates(any(),any());}
 @Test void noActiveProductsRejectsAllScope(){request.setScope(FlashSaleScope.ALL);when(products.lockCampaignCandidates(StatusEnum.Active,null)).thenReturn(List.of());assertThrows(IllegalArgumentException.class,()->service.save(request));verify(campaigns,never()).save(any());}
 @Test void repeatedIdsAreAppliedOnce(){request.setProductIds(List.of(1L,1L));assertEquals(1,service.save(request));}

 @Test void stopAndResumeRestoresOwnedProductsWithoutChangingSchedule(){
  FlashSaleCampaign c=existing();first.setFlashSaleCampaign(c);first.setFlashSaleDiscount(20L);first.setFlashSaleStart(c.getStartsAt());first.setFlashSaleEnd(c.getEndsAt());
  when(products.lockByCampaign(5L)).thenReturn(List.of(first));service.stop(5L);
  assertEquals(10L,first.getEffectiveDiscount());assertSame(c,first.getFlashSaleCampaign());
  assertEquals(1,service.resume(5L));assertTrue(c.isEnabled());assertEquals(20L,first.getEffectiveDiscount());assertEquals(c.getEndsAt(),first.getFlashSaleEnd());
 }
 @Test void expiredOrEmptyCampaignCannotResume(){
  FlashSaleCampaign c=existing();c.setEnabled(false);c.setEndsAt(now.minusMinutes(1));
  assertThrows(IllegalArgumentException.class,()->service.resume(5L));assertFalse(c.isEnabled());
  c.setEndsAt(now.plusDays(1));when(products.lockByCampaign(5L)).thenReturn(List.of());
  assertThrows(IllegalArgumentException.class,()->service.resume(5L));assertFalse(c.isEnabled());
 }
 @Test void resumeSkipsClosedProductsAndDoesNotAdoptTransferredProducts(){
  FlashSaleCampaign c=existing();c.setEnabled(false);first.setFlashSaleCampaign(c);closed.setFlashSaleCampaign(c);closed.setFlashSaleDiscount(0L);
  FlashSaleCampaign other=new FlashSaleCampaign();second.setFlashSaleCampaign(other);second.setFlashSaleDiscount(45L);
  when(products.lockByCampaign(5L)).thenReturn(List.of(first,closed));assertEquals(1,service.resume(5L));
  assertEquals(0L,closed.getFlashSaleDiscount());assertSame(other,second.getFlashSaleCampaign());assertEquals(45L,second.getFlashSaleDiscount());
 }
 @Test void attributionOnlyCountsTheCampaignThatActuallyImprovesThePrice(){
  FlashSaleCampaign c=existing();first.setFlashSaleCampaign(c);first.setFlashSaleDiscount(20L);first.setFlashSaleStart(c.getStartsAt());first.setFlashSaleEnd(c.getEndsAt());
  assertEquals(5L,com.dodostore.dodostore.utils.CampaignAttribution.appliedCampaign(first,20L));
  first.setDiscount(20L);assertNull(com.dodostore.dodostore.utils.CampaignAttribution.appliedCampaign(first,20L));
  first.setDiscount(30L);assertNull(com.dodostore.dodostore.utils.CampaignAttribution.appliedCampaign(first,30L));
  first.setDiscount(10L);c.setEnabled(false);assertNull(com.dodostore.dodostore.utils.CampaignAttribution.appliedCampaign(first,20L));
 }
}
