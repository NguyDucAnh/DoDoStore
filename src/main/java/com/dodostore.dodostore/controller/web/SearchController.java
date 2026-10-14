package com.dodostore.dodostore.controller.web;

import com.dodostore.dodostore.dto.request.SearchRequest;
import com.dodostore.dodostore.dto.response.ProductResponse;
import com.dodostore.dodostore.entity.UserEntity;
import com.dodostore.dodostore.enums.SortEnum;
import com.dodostore.dodostore.enums.StatusEnum;
import com.dodostore.dodostore.service.BrandService;
import com.dodostore.dodostore.service.CartService;
import com.dodostore.dodostore.service.CategoryService;
import com.dodostore.dodostore.service.ProductService;
import com.dodostore.dodostore.utils.GetUserAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class SearchController {
    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CartService cartService;

    @Autowired
    private GetUserAuthentication getUserAuthentication;
    
    @RequestMapping(value = "/home/search", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView search(@ModelAttribute("newSearch") SearchRequest request,
                              @RequestParam(defaultValue = "0") int page) {
        if (request.getTypeSort() == null) request.setTypeSort(SortEnum.NOT_SORT);
        page = Math.max(0, page);
        int size = 8;
        ModelAndView mav = new ModelAndView("/web/search");
        Page<ProductResponse> productPage = productService.searchProduct(request, page, size);

        if (productPage.getTotalPages() > 0 && page >= productPage.getTotalPages()) {
            productPage = productService.searchProduct(request, productPage.getTotalPages() - 1, size);
        }
        mav.addObject("products", productPage.getContent());
        mav.addObject("productPage", productPage);
        mav.addObject("newSearch", request);
        mav.addObject("categories", categoryService.getAllCategoriesPaginated(0,8, "id", "asc",null, StatusEnum.Active));
        mav.addObject("brands", brandService.getAllPaginated(0,8, "id", "asc",null, StatusEnum.Active));
        mav.addObject("sorts", SortEnum.values());

        UserEntity user = getUserAuthentication.getUser();
        if (user != null) {
            mav.addObject("user", user);
            mav.addObject("count", cartService.getCountByUserId(user.getId()));
        }

        return mav;
    }
   
    @PostMapping("/home/search-name")
    public ModelAndView searchProductByName(@RequestParam(required = false) String name) {
        SearchRequest request = new SearchRequest();
        request.setName(name);
        return search(request, 0);
    }

    @GetMapping("/home/search/{brandId}")
    public ModelAndView searchProduct(@PathVariable("brandId") String brandId) {
        SearchRequest request = new SearchRequest();
        request.setBrands(List.of(brandId));
        return search(request, 0);
    }
}
