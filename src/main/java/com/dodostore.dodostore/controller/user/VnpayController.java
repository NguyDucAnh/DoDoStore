package com.dodostore.dodostore.controller.user;

import com.dodostore.dodostore.service.impl.VnpayService;
import com.dodostore.dodostore.utils.GetUserAuthentication;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Controller @RequiredArgsConstructor
@RequestMapping("/payment/vnpay")
public class VnpayController {
    private final VnpayService service;
    private final GetUserAuthentication authentication;

    @GetMapping("/checkout")
    public String checkout(@RequestParam Long addressId, Model model) {
        model.addAttribute("addressId", addressId);
        model.addAttribute("requestKey", UUID.randomUUID().toString());
        return "user/vnpay-checkout";
    }
    @PostMapping("/create")
    public String create(@RequestParam Long addressId, @RequestParam String requestKey,
                         HttpServletRequest request, Model model) {
        try {
            var user = authentication.getUser();
            if (user == null) return "redirect:/login";
            // Do not trust arbitrary X-Forwarded-For. Configure a trusted reverse proxy separately.
            return "redirect:" + service.create(user.getId(), addressId, requestKey, request.getRemoteAddr());
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("message", e.getMessage());
            return "user/vnpay-result";
        }
    }
    private Map<String, String> parameters(HttpServletRequest request) {
        Map<String, String> result = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (key.startsWith("vnp_")) {
                if (values.length != 1 || values[0].length() > 4096) throw new IllegalArgumentException("Tham số không hợp lệ.");
                result.put(key, values[0]);
            }
        });
        return result;
    }
    @GetMapping("/ipn") @ResponseBody
    public Map<String, String> ipn(HttpServletRequest request) {
        try { return service.ipn(parameters(request)); }
        catch (Exception e) {
            // Transaction rollback happens at the service boundary before we acknowledge a retry.
            return VnpayService.reply("99", "System error");
        }
    }
    @GetMapping("/return")
    public String result(HttpServletRequest request, Model model) {
        try { model.addAllAttributes(service.result(parameters(request))); }
        catch (IllegalArgumentException | IllegalStateException e) { model.addAttribute("message", e.getMessage()); }
        return "user/vnpay-result";
    }
}
