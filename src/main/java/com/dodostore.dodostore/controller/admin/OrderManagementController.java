package com.dodostore.dodostore.controller.admin;

import com.dodostore.dodostore.enums.StatusOrderEnum;
import com.dodostore.dodostore.service.OrderDetailService;
import com.dodostore.dodostore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/admin/order")
public class OrderManagementController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;


    @GetMapping()
    public ModelAndView orderManagement(@org.springframework.web.bind.annotation.RequestParam(required=false) StatusOrderEnum status){
        var orders = orderService.getAllOrder().stream()
                .filter(o -> !Boolean.TRUE.equals(o.getCartDraft()))
                .filter(o -> status == null || o.getStatus() == status).toList();
        return new ModelAndView("/admin/order/management")
                .addObject("orders", orders)
                .addObject("selectedStatus", status)
                .addObject("filterStatuses", StatusOrderEnum.values())
                .addObject("filteredCount", orders.size())
                .addObject("filteredTotal", orders.stream().mapToLong(o -> o.getTotal() == null ? 0L : o.getTotal()).sum())
                .addObject("status_orders", java.util.List.of(StatusOrderEnum.Dang_Xu_Ly, StatusOrderEnum.Dang_Giao, StatusOrderEnum.Da_Giao));
    }

    @GetMapping("/{orderId}")
    public ModelAndView getOrderById(@PathVariable Long orderId){
        return new ModelAndView("/admin/order/view")
                .addObject("order", orderService.getOrderById(orderId))
                .addObject("items", orderDetailService.getItemByOrderId(orderId));
    }

    // Older links must not change order data through GET.
    @GetMapping("/{orderId}/{status}")
    public String statusLink() {
        return "redirect:/admin/order";
    }

    @PostMapping("/{orderId}/{status}")
    public String updateStatusOrder(@PathVariable("orderId") Long orderId,
                                    @PathVariable("status") String status,
                                    RedirectAttributes redirectAttributes) {
        try {
            StatusOrderEnum requested;
            try {
                requested = StatusOrderEnum.valueOf(status);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ.");
            }
            orderService.updateStatusOrder(orderId, requested);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái đơn hàng.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/order";
    }
}
