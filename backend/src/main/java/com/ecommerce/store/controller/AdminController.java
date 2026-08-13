package com.ecommerce.store.controller;

import com.ecommerce.store.dto.AdminDtos.CouponRequest;
import com.ecommerce.store.dto.AdminDtos.InventoryUpdateRequest;
import com.ecommerce.store.dto.AdminDtos.OrderAdminUpdateRequest;
import com.ecommerce.store.enums.OrderStatus;
import com.ecommerce.store.service.AdminService;
import com.ecommerce.store.service.AuditService;
import com.ecommerce.store.service.OrderService;
import com.ecommerce.store.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class AdminController {

    private final AdminService adminService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final AuditService auditService;

    public AdminController(
            AdminService adminService,
            OrderService orderService,
            ReviewService reviewService,
            AuditService auditService) {
        this.adminService = adminService;
        this.orderService = orderService;
        this.reviewService = reviewService;
        this.auditService = auditService;
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

    @PostMapping("/orders/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> cancelOrder(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String comment = body != null ? body.get("comment") : null;
        return adminService.cancelOrder(id, comment);
    }

    @PostMapping("/orders/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> refundOrder(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String comment = body != null ? body.get("comment") : null;
        return adminService.refundOrder(id, comment);
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
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createCoupon(@Valid @RequestBody CouponRequest request) {
        return adminService.createCoupon(request);
    }

    @PutMapping("/coupons/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CouponRequest request) {
        return adminService.updateCoupon(id, request);
    }

    @DeleteMapping("/coupons/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCoupon(@PathVariable Long id) {
        adminService.deleteCoupon(id);
    }

    @GetMapping("/reviews/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Map<String, Object>> pendingReviews() {
        return reviewService.listPending();
    }

    @PatchMapping("/reviews/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> approveReview(@PathVariable Long id) {
        return reviewService.approve(id, true);
    }

    @PatchMapping("/reviews/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> rejectReview(@PathVariable Long id) {
        return reviewService.approve(id, false);
    }

    @DeleteMapping("/reviews/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable Long id) {
        reviewService.delete(id);
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> auditLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit) {
        return auditService.list(page, limit);
    }
}
