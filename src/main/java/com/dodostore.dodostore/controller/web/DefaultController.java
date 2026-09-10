package com.dodostore.dodostore.controller.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class DefaultController {
    
    @GetMapping("/default")
    public String getMethodName(HttpServletRequest request) {
        if (request.isUserInRole("ADMIN")) {
            return "redirect:/admin";
        } else 
            return "redirect:/home";
    }
    
}
