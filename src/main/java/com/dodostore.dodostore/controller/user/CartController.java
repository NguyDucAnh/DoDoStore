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
    @org.springframework.web.bind.annotation.ModelAttribute("vnpayRequestKey")
    public String vnpayRequestKey() {
        return java.util.UUID.randomUUID().toString();
    }

    @Autowired
    private CartService cartService;

    @Autowired
    private GetUserAuthentication getUserAuthentication;

    // Resolve the cart owner from the current login for links without a user ID.
    @GetMapping({"", "/"})
    public String currentUserCart() {
        UserEntity user = getUserAuthentication.getUser();
        if (user == null) {
            return "redirect:/login";
        }
        return "redirect:/user/cart/" + user.getId();
    }

    @GetMapping("/{userId}")
    public ModelAndView getCart(@PathVariable Long userId){
        UserEntity user = getUserAuthentication.getUser();

        ModelAndView mav = new ModelAndView("/user/cart")
                .addObject("carts", cartService.getCart(user.getId()))
                .addObject("user", user);

        return mav;
    }

    @PostMapping("/{productId}")
    public ModelAndView addCart(@PathVariable Long productId, @org.springframework.web.bind.annotation.RequestParam(required = false) String size, @org.springframework.web.bind.annotation.RequestParam(required = false) String color, Model model){
        UserEntity user = getUserAuthentication.getUser();
        
        ModelAndView mav = new ModelAndView("/user/cart")
                        .addObject("user", user);
        if(cartService.addCart(user.getId(), productId, size, color)){
            model.addAttribute("notification", "Success");
            model.addAttribute("message", "Thêm Sản Phẩm Thành Công ");
        } else {
            model.addAttribute("notification", "Fail");
            model.addAttribute("message", "Vui lòng chọn màu sắc và size hợp lệ tại trang sản phẩm và kiểm tra số lượng tồn kho.");
        }    
        mav.addObject("carts", cartService.getCart(user.getId()));
        return mav;
    }

    @GetMapping("/{cartId}/{quantity}")
    public ModelAndView replaceQuantityProduct(Model model,@PathVariable("cartId") Long cartId,
                                        @PathVariable("quantity") Long quantity){
                                            
        UserEntity user = getUserAuthentication.getUser();

        ModelAndView mav = new ModelAndView("/user/cart")
                            .addObject("carts", cartService.getCart(user.getId()))
                            .addObject("user", user);
            
        if(cartService.replaceQuantityProduct(user.getId(), cartId, quantity)){
            model.addAttribute("notification", "Success");
            model.addAttribute("message", "Thêm thành công");
        } else {
            model.addAttribute("notification", "Fail");
            model.addAttribute("message", "Không thể mua thêm vì không đủ hàng");
        }
        return mav;
    }

    // AJAX response contains values computed from the signed-in user's cart.
    @PostMapping("/quantity/{cartId}")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<?> updateQuantity(
            @PathVariable Long cartId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long delta,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long quantity) {
        UserEntity user = getUserAuthentication.getUser();
        if (user == null) {
            return org.springframework.http.ResponseEntity.status(401)
                    .body(java.util.Map.of("message", "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."));
        }
        if (quantity != null && quantity < 0) return org.springframework.http.ResponseEntity.badRequest()
                .body(java.util.Map.of("message", "Không được nhập số âm."));
        if ((quantity == null) == (delta == null)) return org.springframework.http.ResponseEntity.badRequest()
                .body(java.util.Map.of("message", "Nhập một số lượng hợp lệ."));
        boolean changed = quantity != null ? cartService.setQuantity(user.getId(), cartId, quantity)
                : cartService.replaceQuantityProduct(user.getId(), cartId, delta);
        if (!changed) {
            return org.springframework.http.ResponseEntity.badRequest()
                    .body(java.util.Map.of("message", "Không thể đổi số lượng: tối thiểu 1 và không vượt quá tồn kho."));
        }
        var carts = cartService.getCart(user.getId());
        var updated = carts.stream().filter(c -> c.getId().equals(cartId))
                .findFirst().orElseThrow();
        long total = carts.stream().mapToLong(c ->
                (c.getProduct().getPrice() * (100 - c.getProduct().getEffectiveDiscount()) / 100) * c.getQuantity()).sum();
        long lineTotal = (updated.getProduct().getPrice() * (100 - updated.getProduct().getEffectiveDiscount()) / 100)
                * updated.getQuantity();
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of(
                "quantity", updated.getQuantity(), "lineTotal", lineTotal, "total", total,
                "stock", updated.getProduct().stockForVariant(updated.getColor(), updated.getSize()),
                "unitPrice", updated.getProduct().getPrice() * (100 - updated.getProduct().getEffectiveDiscount()) / 100,
                "discount", updated.getProduct().getEffectiveDiscount()));
    }

    @GetMapping("/delete/{cartId}")
    public String deleteCart(@PathVariable Long cartId){
        UserEntity user = getUserAuthentication.getUser();
        cartService.removeProductToCart(user.getId(), cartId);
        return "redirect:/user/cart/" + user.getId();
    }
}
