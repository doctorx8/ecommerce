package com.ecommerce.store.controller;

import com.ecommerce.store.config.ApiException;
import com.ecommerce.store.dto.OrderDtos.CheckoutRequest;
import com.ecommerce.store.dto.OrderDtos.StatusUpdateRequest;
import com.ecommerce.store.enums.OrderStatus;
import com.ecommerce.store.security.AuthUser;
import com.ecommerce.store.service.OrderService;
import com.ecommerce.store.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> checkout(@Valid @RequestBody CheckoutRequest request, HttpServletRequest http) {
        return orderService.checkout(request, http.getRemoteAddr());
    }

    @GetMapping("/mine")
    public List<Map<String, Object>> mine() {
        AuthUser user = SecurityUtils.currentUser()
                .filter(AuthUser::isCustomer)
                .orElseThrow(() -> new ApiException("Customer access required", HttpStatus.FORBIDDEN));
        return orderService.myOrders(user.getId());
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) OrderStatus status) {
        return orderService.listOrders(page, limit, status);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> status(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return orderService.updateStatus(id, request);
    }
}
