package com.ecommerce.store.service;

import com.ecommerce.store.config.ApiException;
import com.ecommerce.store.enums.MockPaymentOutcome;
import com.ecommerce.store.enums.PaymentStatus;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class MockPaymentService {

    public record PaymentResult(PaymentStatus status, String paymentRef, String message) {}

    public PaymentResult charge(MockPaymentOutcome outcome, String method) {
        MockPaymentOutcome resolved = outcome != null ? outcome : MockPaymentOutcome.SUCCESS;
        String ref = "mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return switch (resolved) {
            case SUCCESS -> new PaymentResult(
                    PaymentStatus.PAID,
                    ref,
                    "Mock payment succeeded via " + (method != null ? method : "card"));
            case FAIL -> {
                throw new ApiException("Mock payment failed. Choose SUCCESS or PENDING to continue.",
                        HttpStatus.PAYMENT_REQUIRED);
            }
            case PENDING -> new PaymentResult(
                    PaymentStatus.PENDING,
                    ref,
                    "Mock payment pending (COD / delayed capture)");
        };
    }

    public Map<String, Object> describe() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("provider", "mock");
        map.put("outcomes", new String[] {"SUCCESS", "FAIL", "PENDING"});
        map.put("note", "Sandbox only. FAIL aborts checkout; SUCCESS sets PAID; PENDING leaves PENDING.");
        return map;
    }
}
