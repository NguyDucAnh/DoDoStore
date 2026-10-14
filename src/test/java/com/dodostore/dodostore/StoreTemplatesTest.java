package com.dodostore.dodostore;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import org.thymeleaf.context.WebContext;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.dodostore.dodostore.dto.response.ProductResponse;
import com.dodostore.dodostore.enums.StatusOrderEnum;
import static org.junit.jupiter.api.Assertions.*;
class StoreTemplatesTest {
    private String render(String name,Map<String,Object> values) {
        MockServletContext servlet=new MockServletContext();
        MockHttpServletRequest request=new MockHttpServletRequest(servlet);
        request.setRequestURI("/home");
        var exchange=JakartaServletWebApplication.buildApplication(servlet).buildExchange(request,new MockHttpServletResponse());
        var resolver=new ClassLoaderTemplateResolver();resolver.setPrefix("templates/");resolver.setSuffix(".html");resolver.setTemplateMode("HTML");resolver.setCharacterEncoding("UTF-8");
        var engine=new SpringTemplateEngine();engine.setTemplateResolver(resolver);
        var context=new WebContext(exchange,Locale.forLanguageTag("vi"),values);
        if (name.contains(" :: ")) {
            var parts=name.split(" :: ");
            return engine.process(new org.thymeleaf.TemplateSpec(parts[0],Set.of(parts[1]),org.thymeleaf.templatemode.TemplateMode.HTML,null),context);
        }
        return engine.process(name,context);
    }
    @Test void homeRendersSharedHeaderAndActiveFlashSale() throws Exception {
        Map<String,Object> values=new HashMap<>();
        var p=ProductResponse.builder().id(1L).name("Áo polo").price(200000L).discount(30L).flashSaleActive(true)
                .flashSaleEnd(java.time.LocalDateTime.of(2026,12,1,12,0)).images(List.of()).build();
        values.put("flashSales",List.of(p));values.put("brands",List.of());values.put("productSales",List.of());values.put("productDates",List.of());values.put("quantityProduct",0);values.put("headerQuantity",0);
        String html=render("web/home",values);
        java.nio.file.Files.createDirectories(java.nio.file.Path.of("target/qa"));java.nio.file.Files.writeString(java.nio.file.Path.of("target/qa/home.html"),html);
        assertFalse(html.contains("id=\"chatNewConversation\""));
        assertFalse(html.contains("id=\"chatHistoryList\""));
        assertEquals(1,html.split("id=\"chatButton\"",-1).length-1);
        assertTrue(html.contains("FLASH SALE"),"flash section");assertTrue(html.contains("140,000"),"flash price");assertTrue(html.contains("storefront-original"),"shared header");
        assertEquals(1,html.split("class=\"storefront-original\"",-1).length-1);
        java.nio.file.Files.createDirectories(java.nio.file.Path.of("target/qa"));java.nio.file.Files.writeString(java.nio.file.Path.of("target/qa/home.html"),html);
    }
    @Test void dashboardRendersDateFilterAndZeroRevenue() {
        Map<String,Object> values=new HashMap<>();
        values.put("startDate",LocalDate.of(2026,9,1));values.put("endDate",LocalDate.of(2026,9,2));values.put("periodRevenue",BigDecimal.ZERO);
        values.put("revenues",List.of());values.put("productSales",List.of());
        assertTrue(render("admin/admin",values).contains("2026-09-01"));
    }
    @Test void orderFilterRendersAllStatusesAndEmptyMessage() {
        Map<String,Object> values=new HashMap<>();values.put("orders",List.of());values.put("filterStatuses",StatusOrderEnum.values());
        values.put("status_orders",List.of(StatusOrderEnum.Dang_Xu_Ly,StatusOrderEnum.Dang_Giao,StatusOrderEnum.Da_Giao));
        values.put("filteredCount",0);values.put("filteredTotal",0L);values.put("selectedStatus",StatusOrderEnum.Da_Huy);
        String html=render("admin/order/management",values);assertTrue(html.contains("Đã hủy"));assertTrue(html.contains("Không có đơn hàng phù hợp"));
    }
    @Test void campaignPageRendersScopesSelectionAndHistory() throws Exception {
        var request=new com.dodostore.dodostore.dto.request.FlashSaleCampaignRequest();request.setName("Cuối tuần");request.setDiscount(25L);
        request.setStartsAt(java.time.LocalDateTime.now());request.setEndsAt(java.time.LocalDateTime.now().plusDays(1));request.setProductIds(List.of(1L));
        var product=com.dodostore.dodostore.entity.ProductEntity.builder().id(1L).name("Áo polo").price(200000L).quantity(10L).images(List.of()).build();
        var campaign=new com.dodostore.dodostore.entity.FlashSaleCampaign();campaign.setId(1L);campaign.setName("Cuối tuần");campaign.setDiscount(25L);campaign.setScope(com.dodostore.dodostore.enums.FlashSaleScope.SELECTED);campaign.setStartsAt(request.getStartsAt());campaign.setEndsAt(request.getEndsAt());campaign.setProductCount(1);
        String html=render("admin/flash-sale/management",Map.of("campaign",request,"products",List.of(product),"campaigns",List.of(campaign)));
        assertTrue(html.contains("Tất cả sản phẩm đang bán"));assertTrue(html.contains("Áo polo"));assertTrue(html.contains("/admin/flash-sale/1/stop"));assertTrue(html.contains("flash-sale.css"));
        campaign.setEnabled(false);campaign.setEndsAt(java.time.LocalDateTime.now().plusDays(2));campaign.setSoldQuantity(12);
        String stopped=render("admin/flash-sale/management",Map.of("campaign",request,"products",List.of(product),"campaigns",List.of(campaign)));
        assertTrue(stopped.contains("/admin/flash-sale/1/resume"));assertTrue(stopped.contains("Đã bán"));assertTrue(stopped.contains("campaign-sold"));assertFalse(stopped.contains("/admin/flash-sale/1/stop"));
        java.nio.file.Files.createDirectories(java.nio.file.Path.of("target/qa"));java.nio.file.Files.writeString(java.nio.file.Path.of("target/qa/campaign.html"),html);
    }

    @Test void paginationLinksCarryMultipleFiltersAndPageNumber() {
        var filter=new com.dodostore.dodostore.dto.request.SearchRequest();
        filter.setName("Áo & quần");filter.setBrands(List.of("1","2"));filter.setCategories(List.of("3","4"));
        filter.setMinPrice(100L);filter.setMaxPrice(500L);filter.setTypeSort(com.dodostore.dodostore.enums.SortEnum.ORDER_PRICE);
        var page=new org.springframework.data.domain.PageImpl<ProductResponse>(
            Collections.nCopies(8,new ProductResponse()),org.springframework.data.domain.PageRequest.of(0,8),19);
        String html=render("web/search :: nav",Map.of("newSearch",filter,"productPage",page));
        assertTrue(html.contains("page=1"));assertTrue(html.contains("page=2"));
        assertTrue(html.contains("brands=1"));assertTrue(html.contains("brands=2"));
        assertTrue(html.contains("categories=3"));assertTrue(html.contains("categories=4"));
        assertTrue(html.contains("minPrice=100"));assertTrue(html.contains("maxPrice=500"));
        assertTrue(html.contains("typeSort=ORDER_PRICE"));
        assertTrue(html.contains("%26"),"search text must be URL encoded");
    }

    @Test void cartShowsVariantStockAndDiscountedUnitPrice() {
        var brand=new com.dodostore.dodostore.entity.BrandEntity();brand.setName("DoDo");
        var category=new com.dodostore.dodostore.entity.CategoryEntity();category.setName("Áo");
        var product=com.dodostore.dodostore.entity.ProductEntity.builder().id(1L).name("Áo")
            .price(200000L).discount(25L).sizeStock("S:9").variantStock("{\"Đen\":{\"S\":2},\"Trắng\":{\"S\":7}}")
            .images(List.of()).brand(brand).category(category).build();
        var cart=com.dodostore.dodostore.entity.CartEntity.builder().id(1L).product(product).color("Đen").size("S").quantity(2L).build();
        String html=render("user/cart :: .cart-item",Map.of("carts",List.of(cart)));
        assertTrue(html.contains("Tồn kho: 2 sản phẩm"));
        assertTrue(html.contains("200,000"));assertTrue(html.contains("150,000"));assertTrue(html.contains("300,000"));
        assertTrue(html.contains("data-stock=\"2\""));
    }
    @Test void productSelectionScriptReceivesVariantStock() throws Exception {
        var product=ProductResponse.builder().variantStock("{\"Đen\":{\"S\":2,\"M\":0},\"Trắng\":{\"S\":0,\"M\":5}}").sizeStock("S:2,M:5").build();
        String html=render("web/product_detail :: #variant-stock-script",Map.of("product",product));
        assertTrue(html.contains("JSON.parse("));assertFalse(html.contains("/*[["));
        java.nio.file.Files.createDirectories(java.nio.file.Path.of("target/qa"));
        java.nio.file.Files.writeString(java.nio.file.Path.of("target/qa/variant-script.html"),html);
    }

    @Test void customerDetailRendersFieldsAndFormattedHistory() throws Exception {
        var item=new com.dodostore.dodostore.service.impl.CustomerOrderDetailService.Item(3L,"Áo polo","/image/logo.jpg","Đen","M",2L,120000L);
        var detail=new com.dodostore.dodostore.service.impl.CustomerOrderDetailService.Detail(7L,"16:30 22/09/2026","Đang giao","Dang_Giao",120000L,List.of(item));
        String html=render("user/order-detail",Map.of("order",detail));
        assertTrue(html.contains("16:30 22/09/2026"));assertTrue(html.contains("Đang giao"));assertTrue(html.contains("/home/product/3"));assertTrue(html.contains("120,000"));assertTrue(html.contains("/image/logo.jpg"));
        java.nio.file.Files.createDirectories(java.nio.file.Path.of("target/qa"));java.nio.file.Files.writeString(java.nio.file.Path.of("target/qa/order-detail.html"),html);
        var order=new com.dodostore.dodostore.dto.response.OrderResponse();order.setId(7L);order.setDate(Date.from(java.time.Instant.parse("2026-09-22T09:30:00Z")));order.setStatus(StatusOrderEnum.Cho_Thanh_Toan);order.setItems(List.of());order.setTotal(120000L);
        String history=render("user/history",Map.of("orders",List.of(order)));
        assertTrue(history.contains("/user/orders/7"));assertTrue(history.contains("Chờ thanh toán"));assertTrue(history.contains("16:30 22/09/2026"));
    }
}
