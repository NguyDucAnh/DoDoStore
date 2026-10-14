package com.dodostore.dodostore;
import org.junit.jupiter.api.Test;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.data.jpa.repository.Query;
import com.dodostore.dodostore.repository.ProductRepository;
import com.dodostore.dodostore.entity.*;
import com.dodostore.dodostore.enums.*;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class FlashSaleRepositoryTest {
 @Test void campaignLockQueryIncludesUnassignedActiveProductsAndOwnClosedProducts() throws Exception {
  var factory=new LocalContainerEntityManagerFactoryBean();
  factory.setDataSource(new DriverManagerDataSource("jdbc:h2:mem:campaign;MODE=MySQL;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1","sa",""));
  factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());factory.setPackagesToScan("com.dodostore.dodostore.entity");
  factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto","create-drop"));factory.afterPropertiesSet();
  var em=factory.getObject().createEntityManager();
  try {
   em.getTransaction().begin();
   var campaign=new FlashSaleCampaign();campaign.setName("Test");campaign.setDiscount(20L);campaign.setScope(FlashSaleScope.ALL);campaign.setCreatedAt(LocalDateTime.now());campaign.setUpdatedAt(LocalDateTime.now());campaign.setStartsAt(LocalDateTime.now());campaign.setEndsAt(LocalDateTime.now().plusDays(1));em.persist(campaign);
   var active=ProductEntity.builder().name("Active without campaign").status(StatusEnum.Active).quantity(5L).price(100000L).discount(0L).build();
   var closed=ProductEntity.builder().name("Closed with campaign").status(StatusEnum.Closed).flashSaleCampaign(campaign).quantity(3L).price(100000L).discount(0L).build();em.persist(active);em.persist(closed);em.getTransaction().commit();em.clear();
   String query=ProductRepository.class.getMethod("lockCampaignCandidates",StatusEnum.class,Long.class).getAnnotation(Query.class).value();
   em.getTransaction().begin();
   var initial=em.createQuery(query,ProductEntity.class).setParameter("status",StatusEnum.Active).setParameter("campaignId",(Long)null).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
   assertEquals(1,initial.size());assertEquals(active.getId(),initial.get(0).getId());
   var editing=em.createQuery(query,ProductEntity.class).setParameter("status",StatusEnum.Active).setParameter("campaignId",campaign.getId()).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
   assertEquals(2,editing.size());em.getTransaction().commit();
   em.getTransaction().begin();
   for (StatusOrderEnum status : List.of(StatusOrderEnum.Dang_Giao, StatusOrderEnum.Da_Giao, StatusOrderEnum.Cho_Thanh_Toan, StatusOrderEnum.Da_Huy)) {
    var order=new OrderEntity();order.setCartDraft(false);order.setStatus(status);em.persist(order);
    var detail=new OrderDetailEntity();detail.setOrder(order);detail.setProduct(active);detail.setQuantity(3L);detail.setFlashSaleCampaignId(campaign.getId());em.persist(detail);
   }
   var draft=new OrderEntity();draft.setCartDraft(true);draft.setStatus(StatusOrderEnum.Dang_Giao);em.persist(draft);
   var draftLine=new OrderDetailEntity();draftLine.setOrder(draft);draftLine.setQuantity(100L);draftLine.setFlashSaleCampaignId(campaign.getId());em.persist(draftLine);
   var oldOrder=new OrderEntity();oldOrder.setCartDraft(false);oldOrder.setStatus(StatusOrderEnum.Da_Giao);em.persist(oldOrder);
   var oldLine=new OrderDetailEntity();oldLine.setOrder(oldOrder);oldLine.setQuantity(100L);em.persist(oldLine);
   em.flush();
   var repo=new org.springframework.data.jpa.repository.support.JpaRepositoryFactory(em).getRepository(com.dodostore.dodostore.repository.OrderDetailRepository.class);
   var sales=repo.sumCampaignSales(List.of(StatusOrderEnum.Dang_Giao,StatusOrderEnum.Da_Giao));
   assertEquals(1,sales.size());assertEquals(campaign.getId(),sales.get(0).getCampaignId());assertEquals(6L,sales.get(0).getSoldQuantity());
   active.setFlashSaleCampaign(null);campaign.setEnabled(false);em.flush();
   assertEquals(6L,repo.sumCampaignSales(List.of(StatusOrderEnum.Dang_Giao,StatusOrderEnum.Da_Giao)).get(0).getSoldQuantity());
   em.getTransaction().commit();
  } finally { if(em.getTransaction().isActive())em.getTransaction().rollback();em.close();factory.destroy(); }
 }
}
