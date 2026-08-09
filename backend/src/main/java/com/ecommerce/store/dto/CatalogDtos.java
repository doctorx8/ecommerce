package com.ecommerce.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

public final class CatalogDtos {
    private CatalogDtos() {}

    public record CategoryRequest(
            @NotBlank String name,
            String slug,
            String description,
            String image,
            Long parentId,
            Integer sortOrder,
            Boolean active,
            String metaTitle,
            String metaDesc
    ) {}

    public record ManufacturerRequest(
            @NotBlank String name,
            String slug,
            String image,
            Integer sortOrder,
            Boolean active
    ) {}

    public record ProductImageRequest(
            @NotBlank String image,
            String alt,
            Integer sortOrder
    ) {}

    public record ProductRequest(
            @NotBlank String name,
            String slug,
            @NotBlank String sku,
            String model,
            String description,
            String shortDesc,
            @NotNull @PositiveOrZero BigDecimal price,
            BigDecimal compareAtPrice,
            BigDecimal cost,
            Integer quantity,
            String stockStatus,
            BigDecimal weight,
            Integer minimum,
            Boolean subtractStock,
            Boolean active,
            Boolean featured,
            Integer sortOrder,
            Long manufacturerId,
            List<Long> categoryIds,
            List<ProductImageRequest> images,
            String metaTitle,
            String metaDesc
    ) {}
}
