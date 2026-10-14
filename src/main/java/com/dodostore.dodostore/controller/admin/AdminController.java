package com.dodostore.dodostore.controller.admin;

import com.dodostore.dodostore.service.OrderService;
import com.dodostore.dodostore.service.ProductService;
import com.dodostore.dodostore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ModelAndView homeAdmin(
            @org.springframework.web.bind.annotation.RequestParam(required=false) @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
            @org.springframework.web.bind.annotation.RequestParam(required=false) @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        if (endDate == null) endDate = today;
        if (startDate == null) startDate = endDate.minusMonths(1);
        String dateError = null;
        if (startDate.isAfter(endDate) || java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) > 3660) {
            dateError = "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc; khoảng thống kê tối đa 10 năm.";
            startDate = today.minusMonths(1); endDate = today;
        }
        var revenues = orderService.getRevenueByDay(startDate, endDate);
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.US); // Chọn định dạng cho thị trường Mỹ
        String increase = numberFormat.format(orderService.getIncrease());

        return new ModelAndView("/admin/admin")
                .addObject("userQuantity", userService.countUsers())
                .addObject("productQuantity", productService.countProduct())
                .addObject("orderQuantity", orderService.getCount())
                .addObject("increase", increase)
                .addObject("revenues", revenues)
                .addObject("startDate", startDate).addObject("endDate", endDate).addObject("dateError", dateError)
                .addObject("periodRevenue", revenues.stream().map(com.dodostore.dodostore.dto.response.DailyRevenue::getRevenue).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add))
                .addObject("productSales", productService.getProductSale());
    }
}
