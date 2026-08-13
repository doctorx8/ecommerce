package com.ecommerce.store.controller;

import com.ecommerce.store.service.MockPaymentService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final MockPaymentService mockPaymentService;

    public PaymentController(MockPaymentService mockPaymentService) {
        this.mockPaymentService = mockPaymentService;
    }

    @GetMapping("/mock")
    public Map<String, Object> mockInfo() {
        return mockPaymentService.describe();
    }
}
