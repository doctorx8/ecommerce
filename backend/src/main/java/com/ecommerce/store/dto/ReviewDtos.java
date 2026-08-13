package com.ecommerce.store.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class ReviewDtos {
    private ReviewDtos() {}

    public record CreateReviewRequest(
            @NotNull Long productId,
            @NotNull @Min(1) @Max(5) Integer rating,
            @NotBlank String text,
            String author
    ) {}
}
