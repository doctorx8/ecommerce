package com.ecommerce.store.dto;

import com.ecommerce.store.enums.OrderStatus;
import com.ecommerce.store.enums.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public final class AdminDtos {
    private AdminDtos() {}

    public record InventoryUpdateRequest(
            @PositiveOrZero Integer quantity,
            String stockStatus,
            Boolean active
    ) {}

    public record PaymentStatusUpdateRequest(
            @NotNull PaymentStatus paymentStatus
    ) {}

    public record CouponRequest(
            @NotBlank String code,
            @NotBlank String name,
            @NotBlank String type,
            @NotNull @PositiveOrZero BigDecimal discount,
            BigDecimal minOrderTotal,
            Integer maxUses,
            Boolean active
    ) {}

    public record OrderAdminUpdateRequest(
            OrderStatus status,
            PaymentStatus paymentStatus,
            String comment,
            Boolean notifyCustomer
    ) {}
}
