package com.ecommerce.store.controller;

import com.ecommerce.store.dto.AdminDtos.CouponRequest;
import com.ecommerce.store.dto.AdminDtos.InventoryUpdateRequest;
import com.ecommerce.store.dto.AdminDtos.OrderAdminUpdateRequest;
import com.ecommerce.store.enums.OrderStatus;
import com.ecommerce.store.service.AdminService;
import com.ecommerce.store.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final OrderService orderService;

    public AdminController(AdminService adminService, OrderService orderService) {
        this.adminService = adminService;
        this.orderService = orderService;
    }

    @GetMapping("/overview")
    public Map<String, Object> overview() {
        return adminService.overview();
    }

    @GetMapping("/products")
    public Map<String, Object> products(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean lowStock) {
        return adminService.listProducts(page, limit, search, active, lowStock);
    }

    @PatchMapping("/products/{id}/inventory")
    public Map<String, Object> inventory(
            @PathVariable Long id,
            @Valid @RequestBody InventoryUpdateRequest request) {
        return adminService.updateInventory(id, request);
    }

    @GetMapping("/orders")
    public Map<String, Object> orders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) OrderStatus status) {
        return orderService.listOrders(page, limit, status);
    }

    @GetMapping("/orders/{id}")
    public Map<String, Object> order(@PathVariable Long id) {
        return orderService.getAdminOrder(id);
    }

    @PatchMapping("/orders/{id}")
    public Map<String, Object> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderAdminUpdateRequest request) {
        return adminService.updateOrder(id, request);
    }

    @GetMapping("/customers")
    public Map<String, Object> customers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String search) {
        return adminService.listCustomers(page, limit, search);
    }

    @GetMapping("/coupons")
    public List<Map<String, Object>> coupons() {
        return adminService.listCoupons();
    }

    @PostMapping("/coupons")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createCoupon(@Valid @RequestBody CouponRequest request) {
        return adminService.createCoupon(request);
    }

    @PutMapping("/coupons/{id}")
    public Map<String, Object> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CouponRequest request) {
        return adminService.updateCoupon(id, request);
    }

    @DeleteMapping("/coupons/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCoupon(@PathVariable Long id) {
        adminService.deleteCoupon(id);
    }
}
