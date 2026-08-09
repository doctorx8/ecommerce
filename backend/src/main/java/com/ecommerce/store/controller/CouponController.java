package com.ecommerce.store.controller;

import com.ecommerce.store.service.OrderService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private final OrderService orderService;

    public CouponController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/validate/{code}")
    public Map<String, Object> validate(@PathVariable String code) {
        return orderService.validateCouponCode(code);
    }
}
