package com.ecommerce.store.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public final class CartDtos {
    private CartDtos() {}

    public record AddItemRequest(
            @NotNull Long productId,
            @Min(1) int quantity,
            Map<String, String> options,
            String sessionId
    ) {}

    public record UpdateItemRequest(@Min(1) int quantity) {}
}
