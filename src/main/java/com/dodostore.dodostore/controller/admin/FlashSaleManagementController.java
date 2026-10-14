package com.dodostore.dodostore.controller.admin;
import com.dodostore.dodostore.dto.request.FlashSaleCampaignRequest;
import com.dodostore.dodostore.service.impl.FlashSaleCampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller @RequestMapping("/admin/flash-sale") @RequiredArgsConstructor
public class FlashSaleManagementController {
    private final FlashSaleCampaignService service;
    private String page(Model model) {
        model.addAttribute("campaigns",service.list());model.addAttribute("products",service.availableProducts());
        return "admin/flash-sale/management";
    }
    @GetMapping public String index(Model model) {
        FlashSaleCampaignRequest r=new FlashSaleCampaignRequest();
        var now=java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).withSecond(0).withNano(0);
        r.setStartsAt(now);r.setEndsAt(now.plusDays(1));r.setDiscount(20L);
        model.addAttribute("campaign",r);return page(model);
    }
    @GetMapping("/{id}/edit") public String edit(@PathVariable Long id,Model model,RedirectAttributes redirect) {
        try { model.addAttribute("campaign",service.edit(id));return page(model); }
        catch(IllegalArgumentException ex) { redirect.addFlashAttribute("errorMessage",ex.getMessage());return "redirect:/admin/flash-sale"; }
    }
    @PostMapping public String save(@Valid @ModelAttribute("campaign") FlashSaleCampaignRequest request, BindingResult errors, Model model, RedirectAttributes redirect) {
        if(errors.hasErrors()) {
            model.addAttribute("errorMessage","Vui lòng kiểm tra tên, mức giảm, phạm vi và thời gian chiến dịch.");return page(model);
        }
        try {
            int count=service.save(request);redirect.addFlashAttribute("successMessage","Đã lưu chiến dịch cho " + count + " sản phẩm.");
            return "redirect:/admin/flash-sale";
        } catch(IllegalArgumentException ex) { model.addAttribute("errorMessage",ex.getMessage());return page(model); }
    }
    @PostMapping("/{id}/resume") public String resume(@PathVariable Long id,RedirectAttributes redirect) {
        try { int count=service.resume(id);redirect.addFlashAttribute("successMessage","Đã tiếp tục chiến dịch cho " + count + " sản phẩm đang bán còn thuộc chiến dịch. Sản phẩm đã chuyển chiến dịch hoặc ngừng bán không được áp dụng lại."); }
        catch(IllegalArgumentException ex) {redirect.addFlashAttribute("errorMessage",ex.getMessage());}
        return "redirect:/admin/flash-sale";
    }
    @PostMapping("/{id}/stop") public String stop(@PathVariable Long id,RedirectAttributes redirect) {
        try {service.stop(id);redirect.addFlashAttribute("successMessage","Đã dừng chiến dịch. Các sản phẩm liên quan trở về giảm giá thường.");}
        catch(IllegalArgumentException ex) {redirect.addFlashAttribute("errorMessage",ex.getMessage());}
        return "redirect:/admin/flash-sale";
    }
}
