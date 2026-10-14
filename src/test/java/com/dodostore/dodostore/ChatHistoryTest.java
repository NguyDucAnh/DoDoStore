package com.dodostore.dodostore;
import org.junit.jupiter.api.Test;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.web.server.ResponseStatusException;
import com.dodostore.dodostore.repository.*;
import com.dodostore.dodostore.service.impl.ChatHistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ChatHistoryTest {
 @Test void persistsMessagesLimitsToFiveAndKeepsOwnersAndContextsSeparate() {
  var factory=new LocalContainerEntityManagerFactoryBean();
  factory.setDataSource(new DriverManagerDataSource("jdbc:h2:mem:history;MODE=MySQL;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1","sa",""));
  factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());factory.setPackagesToScan("com.dodostore.dodostore.entity");
  factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto","create-drop"));factory.afterPropertiesSet();
  var em=factory.getObject().createEntityManager();
  try {
   var repos=new JpaRepositoryFactory(em);
   var conversations=repos.getRepository(ChatConversationRepository.class);var messages=repos.getRepository(ChatMessageRepository.class);
   var history=new ChatHistoryService(conversations,messages,new ObjectMapper());
   em.getTransaction().begin();
   var first=history.begin("user:1",1L,null,"Tìm áo polo");
   assertEquals(1,first.context().size());
   assertThrows(ResponseStatusException.class,()->history.begin("user:1",1L,first.conversationId(),"Gửi trùng lúc"));
   history.finish("user:1",first,"Áo polo đây",List.of(),false);
   em.getTransaction().commit();em.clear();
   em.getTransaction().begin();
   // A new service instance sees stored data, independent of any in-memory advisor.
   var reloaded=new ChatHistoryService(conversations,messages,new ObjectMapper());
   var thread=reloaded.view("user:1",first.conversationId());assertEquals(2,thread.messages().size());assertEquals("user",thread.messages().get(0).role());assertEquals("Áo polo đây",thread.messages().get(1).content());
   var isolated=history;
   assertThrows(ResponseStatusException.class,()->isolated.view("user:2",first.conversationId()));
   assertThrows(ResponseStatusException.class,()->isolated.begin("user:2",2L,first.conversationId(),"Chiếm hội thoại"));
   var second=history.begin("user:2",2L,null,"Chỉ riêng người hai");history.finish("user:2",second,"Trả lời riêng",List.of(),false);
   var resumed=history.begin("user:1",1L,first.conversationId(),"Có màu đen không?");
   assertEquals(3,resumed.context().size());assertFalse(resumed.context().stream().anyMatch(m->m.content().contains("người hai")));
   history.finish("user:1",resumed,"Có màu đen",List.of(),false);
   for(int i=0;i<6;i++) {var turn=history.begin("user:1",1L,null,"Câu hỏi "+i);history.finish("user:1",turn,"Trả lời "+i,List.of(),false);}
   assertEquals(5,history.recent("user:1").size());assertEquals("Câu hỏi 5",history.recent("user:1").get(0).title());assertEquals(1,history.recent("user:2").size());assertTrue(history.recent("guest:other").isEmpty());
   var continuation=history.begin("user:1",1L,first.conversationId(),"Tiếp tục");history.finish("user:1",continuation,"Lỗi tạm thời",List.of(),true);
   assertEquals(first.conversationId(),history.recent("user:1").get(0).id());
   var afterFailure=history.begin("user:1",1L,first.conversationId(),"Thử lại");assertTrue(afterFailure.context().stream().noneMatch(m->"error".equals(m.role())));history.finish("user:1",afterFailure,"Đã trả lời",List.of(),false);
   em.getTransaction().commit();
  } finally {if(em.getTransaction().isActive())em.getTransaction().rollback();em.close();factory.destroy();}
 }
 @Test void invalidMessagesDoNotCreateConversations() {
  var conversations=org.mockito.Mockito.mock(ChatConversationRepository.class);var messages=org.mockito.Mockito.mock(ChatMessageRepository.class);
  var history=new ChatHistoryService(conversations,messages,new ObjectMapper());
  assertThrows(ResponseStatusException.class,()->history.begin("user:1",1L,null," "));
  assertThrows(ResponseStatusException.class,()->history.begin("user:1",1L,null,"x".repeat(4001)));
  org.mockito.Mockito.verifyNoInteractions(conversations,messages);
 }
}
