package com.dodostore.dodostore;
import org.junit.jupiter.api.Test;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import com.dodostore.dodostore.repository.customer.impl.ProductRepositoryCustomerImpl;
import com.dodostore.dodostore.dto.request.SearchRequest;
import com.dodostore.dodostore.entity.*;
import com.dodostore.dodostore.enums.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class SearchRepositoryPaginationTest {
 @Test void filteredProductsSpanThreePagesWithoutDuplicatesForEverySort() {
  var factory=new LocalContainerEntityManagerFactoryBean();
  factory.setDataSource(new DriverManagerDataSource("jdbc:h2:mem:pagination;MODE=MySQL;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1","sa",""));
  factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());factory.setPackagesToScan("com.dodostore.dodostore.entity");
  factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto","create-drop"));factory.afterPropertiesSet();
  var em=factory.getObject().createEntityManager();
  try {
   em.getTransaction().begin();
   for(int i=0;i<24;i++) em.persist(ProductEntity.builder().name(i<19?"Match":"Other").status(StatusEnum.Active).quantity(5L).price(100L).discount(0L).build());
   em.persist(ProductEntity.builder().name("Match").status(StatusEnum.Closed).quantity(5L).price(100L).discount(0L).build());
   em.getTransaction().commit();em.clear();
   var repository=new ProductRepositoryCustomerImpl();
   ReflectionTestUtils.setField(repository,"entityManager",em);
   var request=new SearchRequest();request.setName("Match");request.setMinPrice(50L);request.setMaxPrice(150L);
   for(var sort:SortEnum.values()) {
    request.setTypeSort(sort);
    Set<Long> ids=new HashSet<>();
    for(int page=0;page<3;page++) {
     var result=repository.search(request,PageRequest.of(page,8));
     assertEquals(19,result.getTotalElements());assertEquals(3,result.getTotalPages());
     assertEquals(page<2?8:3,result.getNumberOfElements());
     result.forEach(product -> assertTrue(ids.add(product.getId())));
    }
    assertEquals(19,ids.size());
   }
  } finally { if(em.getTransaction().isActive())em.getTransaction().rollback();em.close();factory.destroy(); }
 }
}
