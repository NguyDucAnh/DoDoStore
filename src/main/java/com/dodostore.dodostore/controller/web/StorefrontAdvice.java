package com.dodostore.dodostore.controller.web;
import com.dodostore.dodostore.service.CartService;
import com.dodostore.dodostore.utils.GetUserAuthentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
@ControllerAdvice(basePackages={"com.dodostore.dodostore.controller.web","com.dodostore.dodostore.controller.user"})
@RequiredArgsConstructor
public class StorefrontAdvice {
    private final GetUserAuthentication authentication;
    private final CartService carts;
    @ModelAttribute
    public void header(Model model) {
        var user = authentication.getUser();
        model.addAttribute("headerUser", user);
        model.addAttribute("headerQuantity", user == null ? 0 : carts.getCountByUserId(user.getId()));
    }
}
