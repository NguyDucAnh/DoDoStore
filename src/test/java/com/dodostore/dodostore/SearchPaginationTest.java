package com.dodostore.dodostore;

import com.dodostore.dodostore.controller.web.SearchController;
import com.dodostore.dodostore.dto.request.SearchRequest;
import com.dodostore.dodostore.dto.response.ProductResponse;
import com.dodostore.dodostore.enums.SortEnum;
import com.dodostore.dodostore.service.*;
import com.dodostore.dodostore.utils.GetUserAuthentication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SearchPaginationTest {
    SearchController controller;
    ProductService products;
    MockMvc mvc;

    @BeforeEach void setup() {
        controller = new SearchController();
        products = mock(ProductService.class);
        ReflectionTestUtils.setField(controller, "productService", products);
        ReflectionTestUtils.setField(controller, "categoryService", mock(CategoryService.class));
        ReflectionTestUtils.setField(controller, "brandService", mock(BrandService.class));
        ReflectionTestUtils.setField(controller, "cartService", mock(CartService.class));
        ReflectionTestUtils.setField(controller, "getUserAuthentication", mock(GetUserAuthentication.class));
        when(products.searchProduct(any(), anyInt(), eq(8))).thenAnswer(inv ->
            new PageImpl<ProductResponse>((int)inv.getArgument(1) > 2 ? List.of() : java.util.Collections.nCopies((int)inv.getArgument(1)==2 ? 3 : 8, new ProductResponse()), PageRequest.of(inv.getArgument(1), 8), 19));
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test void nextPageKeepsAllFilters() throws Exception {
        mvc.perform(get("/home/search").param("page","1").param("name","Áo")
            .param("minPrice","100").param("maxPrice","500")
            .param("brands","1","2").param("categories","3","4").param("typeSort","ORDER_PRICE"))
            .andExpect(status().isOk()).andExpect(model().attributeExists("products","productPage"));
        var filter = ArgumentCaptor.forClass(SearchRequest.class);
        verify(products).searchProduct(filter.capture(), eq(1), eq(8));
        assertEquals("Áo",filter.getValue().getName());
        assertEquals(100L,filter.getValue().getMinPrice());
        assertEquals(500L,filter.getValue().getMaxPrice());
        assertEquals(List.of("1","2"),filter.getValue().getBrands());
        assertEquals(List.of("3","4"),filter.getValue().getCategories());
        assertEquals(SortEnum.ORDER_PRICE,filter.getValue().getTypeSort());
    }
    @Test void firstVisitProvidesPaginationAndDefaultSort() throws Exception {
        mvc.perform(get("/home/search")).andExpect(status().isOk())
            .andExpect(model().attributeExists("productPage"));
        verify(products).searchProduct(argThat(r -> r.getTypeSort()==SortEnum.NOT_SORT),eq(0),eq(8));
    }
    @Test void legacyNameAndBrandKeepSelection() {
        assertEquals("Áo",((SearchRequest)controller.searchProductByName("Áo").getModel().get("newSearch")).getName());
        assertEquals(List.of("2"),((SearchRequest)controller.searchProduct("2").getModel().get("newSearch")).getBrands());
    }
    @Test void invalidPageReturnsLastAvailablePage() {
        var result=controller.search(new SearchRequest(),99);
        assertEquals(2,((Page<?>)result.getModel().get("productPage")).getNumber());
        verify(products).searchProduct(any(),eq(2),eq(8));
    }
    @Test void negativePageReturnsFirstPage() {
        controller.search(new SearchRequest(),-1);
        verify(products).searchProduct(any(),eq(0),eq(8));
    }
}
