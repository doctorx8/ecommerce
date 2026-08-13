package com.ecommerce.store.service;

import com.ecommerce.store.config.ApiException;
import com.ecommerce.store.dto.AdminDtos.CouponRequest;
import com.ecommerce.store.dto.AdminDtos.InventoryUpdateRequest;
import com.ecommerce.store.dto.AdminDtos.OrderAdminUpdateRequest;
import com.ecommerce.store.entity.*;
import com.ecommerce.store.enums.OrderStatus;
import com.ecommerce.store.enums.PaymentStatus;
import com.ecommerce.store.enums.StockStatus;
import com.ecommerce.store.repository.*;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final CouponRepository couponRepository;
    private final CategoryRepository categoryRepository;
    private final OrderService orderService;
    private final OrderStatusRules orderStatusRules;
    private final EmailService emailService;
    private final AuditService auditService;

    public AdminService(
            ProductRepository productRepository,
            OrderRepository orderRepository,
            CustomerRepository customerRepository,
            CouponRepository couponRepository,
            CategoryRepository categoryRepository,
            OrderService orderService,
            OrderStatusRules orderStatusRules,
            EmailService emailService,
            AuditService auditService) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.couponRepository = couponRepository;
        this.categoryRepository = categoryRepository;
        this.orderService = orderService;
        this.orderStatusRules = orderStatusRules;
        this.emailService = emailService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> overview() {
        long products = productRepository.count();
        long activeProducts = productRepository.countByActiveTrue();
        long lowStock = productRepository.countByActiveTrueAndQuantityLessThanEqual(5);
        long outOfStock = productRepository.countByStockStatus(StockStatus.OUT_OF_STOCK);
        long customers = customerRepository.count();
        long coupons = couponRepository.count();
        long orders = orderRepository.count();
        BigDecimal revenue = orderRepository.sumRevenueExcludingCancelled();

        Map<String, Long> ordersByStatus = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            ordersByStatus.put(status.name(), orderRepository.countByStatus(status));
        }

        List<Map<String, Object>> recentOrders = orderRepository
                .findAll(PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent()
                .stream()
                .map(orderService::toAdminOrderMap)
                .toList();

        List<Map<String, Object>> lowStockProducts = productRepository
                .findByActiveTrueAndQuantityLessThanEqualOrderByQuantityAsc(5, PageRequest.of(0, 8))
                .stream()
                .map(this::toInventoryMap)
                .toList();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("products", products);
        map.put("activeProducts", activeProducts);
        map.put("lowStock", lowStock);
        map.put("outOfStock", outOfStock);
        map.put("customers", customers);
        map.put("coupons", coupons);
        map.put("orders", orders);
        map.put("revenue", revenue != null ? revenue : BigDecimal.ZERO);
        map.put("ordersByStatus", ordersByStatus);
        map.put("recentOrders", recentOrders);
        map.put("lowStockProducts", lowStockProducts);
        map.put("categories", categoryRepository.count());
        map.put("salesOverTime", salesOverTime());
        return map;
    }

    private List<Map<String, Object>> salesOverTime() {
        List<Map<String, Object>> series = new ArrayList<>();
        for (Object[] row : orderRepository.salesLast14Days()) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", String.valueOf(row[0]));
            point.put("revenue", row[1]);
            point.put("orders", row[2]);
            series.add(point);
        }
        return series;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listProducts(int page, int limit, String search, Boolean active, Boolean lowStock) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("sku")), like),
                        cb.like(cb.lower(root.get("model")), like)
                ));
            }
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }
            if (Boolean.TRUE.equals(lowStock)) {
                predicates.add(cb.lessThanOrEqualTo(root.get("quantity"), 5));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(Predicate[]::new));
        };

        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.min(Math.max(limit, 1), 100),
                Sort.by(Sort.Direction.ASC, "name"));
        Page<Product> result = productRepository.findAll(spec, pageable);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", result.getContent().stream().map(this::toInventoryMap).toList());
        response.put("page", page);
        response.put("limit", pageable.getPageSize());
        response.put("total", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        return response;
    }

    @Transactional
    public Map<String, Object> updateInventory(Long id, InventoryUpdateRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));
        if (req.quantity() != null) {
            product.setQuantity(req.quantity());
        }
        if (req.stockStatus() != null && !req.stockStatus().isBlank()) {
            product.setStockStatus(StockStatus.valueOf(req.stockStatus()));
        } else if (req.quantity() != null) {
            product.setStockStatus(req.quantity() > 0 ? StockStatus.IN_STOCK : StockStatus.OUT_OF_STOCK);
        }
        if (req.active() != null) {
            product.setActive(req.active());
        }
        Product saved = productRepository.save(product);
        auditService.record("INVENTORY_UPDATE", "Product", saved.getId(),
                "qty=" + saved.getQuantity() + ",status=" + saved.getStockStatus());
        return toInventoryMap(saved);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listCustomers(int page, int limit, String search) {
        Specification<Customer> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }
            String like = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("firstName")), like),
                    cb.like(cb.lower(root.get("lastName")), like)
            );
        };
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.min(Math.max(limit, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Customer> result = customerRepository.findAll(spec, pageable);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", result.getContent().stream().map(this::toCustomerMap).toList());
        response.put("page", page);
        response.put("limit", pageable.getPageSize());
        response.put("total", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        return response;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listCoupons() {
        return couponRepository.findAll(Sort.by(Sort.Direction.ASC, "code"))
                .stream()
                .map(this::toCouponMap)
                .toList();
    }

    @Transactional
    public Map<String, Object> createCoupon(CouponRequest req) {
        String code = req.code().trim().toUpperCase();
        if (couponRepository.findByCode(code).isPresent()) {
            throw new ApiException("Coupon code already exists", HttpStatus.CONFLICT);
        }
        Coupon coupon = new Coupon();
        applyCoupon(coupon, req, code);
        Coupon saved = couponRepository.save(coupon);
        auditService.record("COUPON_CREATE", "Coupon", saved.getId(), saved.getCode());
        return toCouponMap(saved);
    }

    @Transactional
    public Map<String, Object> updateCoupon(Long id, CouponRequest req) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ApiException("Coupon not found", HttpStatus.NOT_FOUND));
        String code = req.code().trim().toUpperCase();
        couponRepository.findByCode(code).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ApiException("Coupon code already exists", HttpStatus.CONFLICT);
            }
        });
        applyCoupon(coupon, req, code);
        Coupon saved = couponRepository.save(coupon);
        auditService.record("COUPON_UPDATE", "Coupon", saved.getId(), saved.getCode());
        return toCouponMap(saved);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new ApiException("Coupon not found", HttpStatus.NOT_FOUND);
        }
        couponRepository.deleteById(id);
        auditService.record("COUPON_DELETE", "Coupon", id, null);
    }

    @Transactional
    public Map<String, Object> updateOrder(Long id, OrderAdminUpdateRequest req) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));
        if (req.status() != null && req.status() != order.getStatus()) {
            orderStatusRules.assertTransition(order.getStatus(), req.status());
            order.setStatus(req.status());
            OrderHistory history = new OrderHistory();
            history.setOrder(order);
            history.setStatus(req.status());
            history.setComment(req.comment());
            history.setNotify(Boolean.TRUE.equals(req.notifyCustomer()));
            order.getHistory().add(history);
            if (Boolean.TRUE.equals(req.notifyCustomer())
                    || req.status() == OrderStatus.SHIPPED
                    || req.status() == OrderStatus.DELIVERED) {
                emailService.sendShippingUpdate(
                        order.getEmail(), order.getOrderNumber(), req.status().name(), req.comment());
            }
        }
        if (req.paymentStatus() != null) {
            order.setPaymentStatus(req.paymentStatus());
        }
        Order saved = orderRepository.save(order);
        auditService.record("ORDER_UPDATE", "Order", saved.getId(),
                "status=" + saved.getStatus() + ",payment=" + saved.getPaymentStatus());
        return orderService.toAdminOrderMap(saved);
    }

    @Transactional
    public Map<String, Object> cancelOrder(Long id, String comment) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));
        if (!orderStatusRules.canCancel(order.getStatus())) {
            throw new ApiException("Order cannot be cancelled in status " + order.getStatus(), HttpStatus.BAD_REQUEST);
        }
        orderStatusRules.assertTransition(order.getStatus(), OrderStatus.CANCELLED);
        order.setStatus(OrderStatus.CANCELLED);
        OrderHistory history = new OrderHistory();
        history.setOrder(order);
        history.setStatus(OrderStatus.CANCELLED);
        history.setComment(comment != null ? comment : "Cancelled by admin");
        history.setNotify(true);
        order.getHistory().add(history);
        Order saved = orderRepository.save(order);
        emailService.sendShippingUpdate(saved.getEmail(), saved.getOrderNumber(), "CANCELLED", history.getComment());
        auditService.record("ORDER_CANCEL", "Order", id, history.getComment());
        return orderService.toAdminOrderMap(saved);
    }

    @Transactional
    public Map<String, Object> refundOrder(Long id, String comment) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));
        if (order.getStatus() == OrderStatus.REFUNDED) {
            throw new ApiException("Order already refunded", HttpStatus.BAD_REQUEST);
        }
        if (!orderStatusRules.canRefund(order.getStatus()) && order.getStatus() != OrderStatus.CANCELLED) {
            throw new ApiException("Order cannot be refunded in status " + order.getStatus(), HttpStatus.BAD_REQUEST);
        }
        orderStatusRules.assertTransition(order.getStatus(), OrderStatus.REFUNDED);
        order.setStatus(OrderStatus.REFUNDED);
        order.setPaymentStatus(PaymentStatus.REFUNDED);
        OrderHistory history = new OrderHistory();
        history.setOrder(order);
        history.setStatus(OrderStatus.REFUNDED);
        history.setComment(comment != null ? comment : "Refunded by admin");
        history.setNotify(true);
        order.getHistory().add(history);
        Order saved = orderRepository.save(order);
        emailService.sendShippingUpdate(saved.getEmail(), saved.getOrderNumber(), "REFUNDED", history.getComment());
        auditService.record("ORDER_REFUND", "Order", id, history.getComment());
        return orderService.toAdminOrderMap(saved);
    }

    private void applyCoupon(Coupon coupon, CouponRequest req, String code) {
        String type = req.type().trim().toUpperCase();
        if (!"PERCENT".equals(type) && !"FIXED".equals(type)) {
            throw new ApiException("Coupon type must be PERCENT or FIXED", HttpStatus.BAD_REQUEST);
        }
        coupon.setCode(code);
        coupon.setName(req.name().trim());
        coupon.setType(type);
        coupon.setDiscount(req.discount());
        coupon.setMinOrderTotal(req.minOrderTotal());
        coupon.setMaxUses(req.maxUses());
        coupon.setActive(req.active() == null || req.active());
    }

    private Map<String, Object> toInventoryMap(Product p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("name", p.getName());
        map.put("slug", p.getSlug());
        map.put("sku", p.getSku());
        map.put("model", p.getModel());
        map.put("price", p.getPrice());
        map.put("quantity", p.getQuantity());
        map.put("stockStatus", p.getStockStatus().name());
        map.put("isActive", p.isActive());
        map.put("isFeatured", p.isFeatured());
        map.put("subtractStock", p.isSubtractStock());
        map.put("updatedAt", p.getUpdatedAt());
        return map;
    }

    private Map<String, Object> toCustomerMap(Customer c) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        map.put("email", c.getEmail());
        map.put("firstName", c.getFirstName());
        map.put("lastName", c.getLastName());
        map.put("telephone", c.getTelephone());
        map.put("isActive", c.isActive());
        map.put("newsletter", c.isNewsletter());
        map.put("createdAt", c.getCreatedAt());
        map.put("orderCount", orderRepository.countByCustomerId(c.getId()));
        return map;
    }

    private Map<String, Object> toCouponMap(Coupon c) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        map.put("code", c.getCode());
        map.put("name", c.getName());
        map.put("type", c.getType());
        map.put("discount", c.getDiscount());
        map.put("minOrderTotal", c.getMinOrderTotal());
        map.put("maxUses", c.getMaxUses());
        map.put("usedCount", c.getUsedCount());
        map.put("isActive", c.isActive());
        map.put("startsAt", c.getStartsAt());
        map.put("endsAt", c.getEndsAt());
        return map;
    }
}
