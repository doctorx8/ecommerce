package com.ecommerce.store.dto;

import com.ecommerce.store.enums.MockPaymentOutcome;
import com.ecommerce.store.enums.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public final class OrderDtos {
    private OrderDtos() {}

    public record AddressPayload(
            @NotBlank String firstName,
            @NotBlank String lastName,
            String company,
            @NotBlank String address1,
            String address2,
            @NotBlank String city,
            @NotBlank String postcode,
            @NotBlank String country,
            String zone
    ) {}

    public record CheckoutRequest(
            String sessionId,
            @Email String email,
            String telephone,
            String firstName,
            String lastName,
            @Valid @NotNull AddressPayload shipping,
            @Valid AddressPayload billing,
            String paymentMethod,
            String shippingMethod,
            @PositiveOrZero BigDecimal shippingCost,
            String couponCode,
            String comment,
            MockPaymentOutcome paymentOutcome
    ) {}

    public record QuoteRequest(
            String sessionId,
            String couponCode
    ) {}

    public record StatusUpdateRequest(
            @NotNull OrderStatus status,
            String comment,
            Boolean notifyCustomer
    ) {}
}
