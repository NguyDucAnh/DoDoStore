package com.dodostore.dodostore.controller.web;

import com.dodostore.dodostore.dto.request.CreateUserRequest;
import com.dodostore.dodostore.dto.request.LoginRequest;
import com.dodostore.dodostore.exception.ErrorCode;
import com.dodostore.dodostore.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AuthenticationController {
    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public ModelAndView login() {
        return new ModelAndView("web/login")
                   .addObject("login", new LoginRequest());
    }


    @GetMapping("/register")
    public ModelAndView register() {
        return new ModelAndView("web/register")
                .addObject("newUser", new CreateUserRequest());
    }

    @PostMapping("/register")
    public String register(@org.springframework.validation.annotation.Validated({jakarta.validation.groups.Default.class, com.dodostore.dodostore.dto.request.Registration.class}) @ModelAttribute("newUser") CreateUserRequest newUser, BindingResult result, Model model) {
        if(result.hasErrors()){
            String enumKey = result.getFieldError().getDefaultMessage();
            String message;
            try { message = ErrorCode.valueOf(enumKey).getMessage(); }
            catch (IllegalArgumentException ex) { message = enumKey; }
            model.addAttribute("message", message);
            return "web/register";
        }
        try {
            userService.registerUser(newUser);
            model.addAttribute("message", "Đăng ký thành công. Vui lòng đăng nhập.");
            model.addAttribute("login", new LoginRequest());
            return "/web/login";
        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
            return "web/register";
        }
    }
}
