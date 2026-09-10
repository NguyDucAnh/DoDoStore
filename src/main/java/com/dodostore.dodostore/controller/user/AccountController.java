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

    @GetMapping("/history")
    public ModelAndView historyBuyProduct(){
        UserEntity user = getUserAuthentication.getUser();
        
        return new ModelAndView("/user/history")
                .addObject("orders", orderService.historyBuy(user.getId()));
    }
}
