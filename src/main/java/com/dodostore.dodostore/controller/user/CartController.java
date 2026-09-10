package com.dodostore.dodostore.controller.user;

import com.dodostore.dodostore.entity.UserEntity;
import com.dodostore.dodostore.service.CartService;
import com.dodostore.dodostore.utils.GetUserAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("user/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @Autowired
    private GetUserAuthentication getUserAuthentication;

    @GetMapping("/{userId}")
    public ModelAndView getCart(@PathVariable Long userId){
        UserEntity user = getUserAuthentication.getUser();

        ModelAndView mav = new ModelAndView("/user/cart")
                .addObject("carts", cartService.getCart(userId))
                .addObject("user", user);

        return mav;
    }

    @PostMapping("/{productId}")
    public ModelAndView addCart(@PathVariable Long productId, Model model){
        UserEntity user = getUserAuthentication.getUser();
        
        ModelAndView mav = new ModelAndView("/user/cart")
                        .addObject("user", user);
        if(cartService.addCart(user.getId(), productId)){
            model.addAttribute("notification", "Success");
            model.addAttribute("message", "Thêm Sản Phẩm Thành Công ");
        } else {
            model.addAttribute("notification", "Fail");
            model.addAttribute("message", "Sản Phẩm Này Đã Có Trong Giỏ Hàng Rồi");
        }    
        mav.addObject("carts", cartService.getCart(user.getId()));
        return mav;
    }

    @GetMapping("/{productId}/{quantity}")
    public ModelAndView replaceQuantityProduct(Model model,@PathVariable("productId") Long productId,
                                        @PathVariable("quantity") Long quantity){
                                            
        UserEntity user = getUserAuthentication.getUser();

        ModelAndView mav = new ModelAndView("/user/cart")
                            .addObject("carts", cartService.getCart(user.getId()))
                            .addObject("user", user);
            
        if(cartService.replaceQuantityProduct(user.getId(), productId, quantity)){
            model.addAttribute("notification", "Success");
            model.addAttribute("message", "Thêm thành công");
        } else {
            model.addAttribute("notification", "Fail");
            model.addAttribute("message", "Không thể mua thêm vì không đủ hàng");
        }
        return mav;
    }

    // AJAX response contains values computed from the signed-in user's cart.
    @PostMapping("/quantity/{productId}")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<?> updateQuantity(
            @PathVariable Long productId,
            @org.springframework.web.bind.annotation.RequestParam Long delta) {
        UserEntity user = getUserAuthentication.getUser();
        if (user == null) {
            return org.springframework.http.ResponseEntity.status(401)
                    .body(java.util.Map.of("message", "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."));
        }
        if (!cartService.replaceQuantityProduct(user.getId(), productId, delta)) {
            return org.springframework.http.ResponseEntity.badRequest()
                    .body(java.util.Map.of("message", "Không thể đổi số lượng: tối thiểu 1 và không vượt quá tồn kho."));
        }
        var carts = cartService.getCart(user.getId());
        var updated = carts.stream().filter(c -> c.getProduct().getId().equals(productId))
                .findFirst().orElseThrow();
        long total = carts.stream().mapToLong(c ->
                (c.getProduct().getPrice() * (100 - c.getProduct().getDiscount()) / 100) * c.getQuantity()).sum();
        long lineTotal = (updated.getProduct().getPrice() * (100 - updated.getProduct().getDiscount()) / 100)
                * updated.getQuantity();
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of(
                "quantity", updated.getQuantity(), "lineTotal", lineTotal, "total", total));
    }

    @GetMapping("/delete/{productId}")
    public String deleteCart(@PathVariable Long productId){
        UserEntity user = getUserAuthentication.getUser();
        cartService.removeProductToCart(user.getId(), productId);
        return "redirect:/user/cart/" + user.getId();
    }
}
