package com.dodostore.dodostore.controller.user;

import com.dodostore.dodostore.dto.request.AddressRequest;
import com.dodostore.dodostore.dto.request.CreateUserRequest;
import com.dodostore.dodostore.dto.request.UpdateUserRequest;
import com.dodostore.dodostore.entity.UserEntity;
import com.dodostore.dodostore.enums.GenderEnum;
import com.dodostore.dodostore.service.CartService;
import com.dodostore.dodostore.service.OrderService;
import com.dodostore.dodostore.service.UserService;
import com.dodostore.dodostore.utils.GetUserAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/user")
public class AccountController {   
    @ModelAttribute("vnpayRequestKey")
    public String vnpayRequestKey() {
        return java.util.UUID.randomUUID().toString();
    }

    @Autowired
    private GetUserAuthentication getUserAuthentication;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/info")
    public ModelAndView viewUser(){
        ModelAndView mav =  new ModelAndView("/user/information")
                .addObject("genders", GenderEnum.values())
                .addObject("updateUser", new CreateUserRequest())
                .addObject("newAddress", new AddressRequest());
        
        UserEntity user = getUserAuthentication.getUser();
        if(user != null){
            mav.addObject("user", user);
        }

        return mav;
    }

    @PostMapping("/update")
    public String updateUser(@ModelAttribute("updateUser") UpdateUserRequest request,
                             @RequestParam("file") MultipartFile file){
        
        UserEntity user = getUserAuthentication.getUser();

        userService.updateUser(request, user.getId(), file);

        return "redirect:/user/info";
    }

    @PostMapping("/add-address")
    public String updateUser(@ModelAttribute("newAddress") AddressRequest request){
        
        UserEntity user = getUserAuthentication.getUser();

        userService.addAddress(user.getId(), request);

        return "redirect:/user/info";
    }

    @GetMapping("/delete-address/{addressId}")
    public String deleteAddress(@PathVariable Long addressId){
        userService.deleteAddress(addressId);

        return "redirect:/user/info";
    }

    @GetMapping("/checkout/{addressId}")
    public ModelAndView checkout(@PathVariable Long addressId, Model model){
        UserEntity user = getUserAuthentication.getUser();

        String message = userService.checkout(user.getId(), addressId);
        model.addAttribute("message", message);

        return new ModelAndView("/user/cart")
                .addObject("carts", cartService.getCart(user.getId()))
                .addObject("user", user);
    }

    @PostMapping("/orders/{orderId}/received")
    public String confirmReceived(@PathVariable Long orderId,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        UserEntity user = getUserAuthentication.getUser();
        if (user == null) return "redirect:/login";
        try {
            orderService.confirmReceived(user.getId(), orderId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận nhận hàng. Trạng thái đơn hàng: Đã giao.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/user/history";
    }

    @Autowired
    private com.dodostore.dodostore.service.impl.CustomerOrderDetailService customerOrderDetails;

    @GetMapping("/orders/{orderId}")
    public ModelAndView orderDetail(@PathVariable Long orderId) {
        UserEntity user=getUserAuthentication.getUser();
        if(user==null) return new ModelAndView("redirect:/login");
        return new ModelAndView("user/order-detail").addObject("order",customerOrderDetails.view(user.getId(),orderId)).addObject("user",user);
    }

    @GetMapping("/history")
    public ModelAndView historyBuyProduct(){
        UserEntity user = getUserAuthentication.getUser();
        
        if (user == null) return new ModelAndView("redirect:/login");
        return new ModelAndView("/user/history")
                .addObject("orders", orderService.historyBuy(user.getId()));
    }
}
