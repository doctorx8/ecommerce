package com.ecommerce.store.controller;

import com.ecommerce.store.dto.CatalogDtos.*;
import com.ecommerce.store.service.CatalogService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/categories")
    public Object categories(@RequestParam(defaultValue = "false") boolean tree) {
        return catalogService.listCategories(tree);
    }

    @GetMapping("/categories/{idOrSlug}")
    public Map<String, Object> category(@PathVariable String idOrSlug) {
        return catalogService.getCategory(idOrSlug);
    }

    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createCategory(@Valid @RequestBody CategoryRequest request) {
        return catalogService.createCategory(request);
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return catalogService.updateCategory(id, request);
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        catalogService.deleteCategory(id);
    }

    @GetMapping("/manufacturers")
    public List<Map<String, Object>> manufacturers() {
        return catalogService.listManufacturers();
    }

    @GetMapping("/manufacturers/{idOrSlug}")
    public Map<String, Object> manufacturer(@PathVariable String idOrSlug) {
        return catalogService.getManufacturer(idOrSlug);
    }

    @PostMapping("/manufacturers")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createManufacturer(@Valid @RequestBody ManufacturerRequest request) {
        return catalogService.createManufacturer(request);
    }

    @GetMapping("/products")
    public Map<String, Object> products(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return catalogService.listProducts(page, limit, search, category, manufacturer, featured, sort, minPrice, maxPrice);
    }

    @GetMapping("/products/{idOrSlug}")
    public Map<String, Object> product(@PathVariable String idOrSlug) {
        return catalogService.getProduct(idOrSlug);
    }

    @PostMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createProduct(@Valid @RequestBody ProductRequest request) {
        return catalogService.createProduct(request);
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return catalogService.updateProduct(id, request);
    }

    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        catalogService.deleteProduct(id);
    }
}
