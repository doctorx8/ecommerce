package com.ecommerce.store.service;

import com.ecommerce.store.config.ApiException;
import com.ecommerce.store.dto.OrderDtos.AddressPayload;
import com.ecommerce.store.dto.OrderDtos.CheckoutRequest;
import com.ecommerce.store.dto.OrderDtos.StatusUpdateRequest;
import com.ecommerce.store.entity.*;
import com.ecommerce.store.enums.OrderStatus;
import com.ecommerce.store.enums.PaymentStatus;
import com.ecommerce.store.enums.StockStatus;
import com.ecommerce.store.repository.*;
import com.ecommerce.store.security.AuthUser;
import com.ecommerce.store.service.CartService.Owner;
import com.ecommerce.store.util.OrderNumberUtils;
import com.ecommerce.store.util.SecurityUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final CouponRepository couponRepository;
    private final ProductRepository productRepository;

    public OrderService(
            OrderRepository orderRepository,
            CartService cartService,
            CartItemRepository cartItemRepository,
            CustomerRepository customerRepository,
            CouponRepository couponRepository,
            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.cartItemRepository = cartItemRepository;
        this.customerRepository = customerRepository;
        this.couponRepository = couponRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Map<String, Object> checkout(CheckoutRequest req, String ipAddress) {
        AuthUser user = SecurityUtils.currentUser().orElse(null);
        Owner owner = user != null && user.isCustomer()
                ? new Owner(user.getId(), null)
                : cartService.resolveOwner(req.sessionId());

        List<CartItem> cartItems = cartService.loadItems(owner);
        if (cartItems.isEmpty()) {
            throw new ApiException("Cart is empty", HttpStatus.BAD_REQUEST);
        }

        Customer customer = null;
        if (owner.customerId() != null) {
            customer = customerRepository.findById(owner.customerId())
                    .orElseThrow(() -> new ApiException("Customer not found", HttpStatus.NOT_FOUND));
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            if (!product.isActive()) {
                throw new ApiException("Product " + product.getName() + " is unavailable", HttpStatus.BAD_REQUEST);
            }
            if (product.isSubtractStock() && product.getQuantity() < item.getQuantity()) {
                throw new ApiException("Insufficient stock for " + product.getName(), HttpStatus.BAD_REQUEST);
            }
            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        BigDecimal discountTotal = BigDecimal.ZERO;
        Coupon coupon = null;
        if (req.couponCode() != null && !req.couponCode().isBlank()) {
            coupon = validateCoupon(req.couponCode(), subtotal);
            if ("PERCENT".equalsIgnoreCase(coupon.getType())) {
                discountTotal = subtotal.multiply(coupon.getDiscount())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } else {
                discountTotal = coupon.getDiscount();
            }
            if (discountTotal.compareTo(subtotal) > 0) {
                discountTotal = subtotal;
            }
        }

        BigDecimal shippingCost = req.shippingCost() != null ? req.shippingCost() : BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        BigDecimal total = subtotal.add(shippingCost).add(taxTotal).subtract(discountTotal);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        AddressPayload shipping = req.shipping();
        AddressPayload billing = req.billing() != null ? req.billing() : shipping;

        String email = customer != null ? customer.getEmail() : req.email();
        String firstName = customer != null ? customer.getFirstName()
                : (req.firstName() != null ? req.firstName() : shipping.firstName());
        String lastName = customer != null ? customer.getLastName()
                : (req.lastName() != null ? req.lastName() : shipping.lastName());
        String telephone = customer != null ? customer.getTelephone() : req.telephone();
        if (email == null || email.isBlank()) {
            throw new ApiException("Email is required", HttpStatus.BAD_REQUEST);
        }

        Order order = new Order();
        order.setOrderNumber(OrderNumberUtils.generate());
        order.setCustomer(customer);
        order.setCoupon(coupon);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setPaymentMethod(req.paymentMethod() != null ? req.paymentMethod() : "cod");
        order.setShippingMethod(req.shippingMethod() != null ? req.shippingMethod() : "flat");
        order.setEmail(email);
        order.setTelephone(telephone);
        order.setFirstName(firstName);
        order.setLastName(lastName);
        order.setShippingFirstName(shipping.firstName());
        order.setShippingLastName(shipping.lastName());
        order.setShippingCompany(shipping.company());
        order.setShippingAddress1(shipping.address1());
        order.setShippingAddress2(shipping.address2());
        order.setShippingCity(shipping.city());
        order.setShippingPostcode(shipping.postcode());
        order.setShippingCountry(shipping.country());
        order.setShippingZone(shipping.zone());
        order.setBillingFirstName(billing.firstName());
        order.setBillingLastName(billing.lastName());
        order.setBillingCompany(billing.company());
        order.setBillingAddress1(billing.address1());
        order.setBillingAddress2(billing.address2());
        order.setBillingCity(billing.city());
        order.setBillingPostcode(billing.postcode());
        order.setBillingCountry(billing.country());
        order.setBillingZone(billing.zone());
        order.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        order.setShippingCost(shippingCost.setScale(2, RoundingMode.HALF_UP));
        order.setDiscountTotal(discountTotal.setScale(2, RoundingMode.HALF_UP));
        order.setTaxTotal(taxTotal);
        order.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        order.setComment(req.comment());
        order.setIpAddress(ipAddress);

        for (CartItem item : cartItems) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(item.getProduct());
            oi.setName(item.getProduct().getName());
            oi.setSku(item.getProduct().getSku());
            oi.setModel(item.getProduct().getModel());
            oi.setQuantity(item.getQuantity());
            oi.setPrice(item.getProduct().getPrice());
            oi.setTotal(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            oi.setOptionsJson(item.getOptionsJson());
            order.getItems().add(oi);

            if (item.getProduct().isSubtractStock()) {
                Product p = item.getProduct();
                p.setQuantity(p.getQuantity() - item.getQuantity());
                if (p.getQuantity() <= 0) {
                    p.setStockStatus(StockStatus.OUT_OF_STOCK);
                }
                productRepository.save(p);
            }
        }

        OrderHistory history = new OrderHistory();
        history.setOrder(order);
        history.setStatus(OrderStatus.PENDING);
        history.setComment("Order placed");
        order.getHistory().add(history);

        orderRepository.save(order);

        if (coupon != null) {
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);
        }

        if (owner.customerId() != null) {
            cartItemRepository.deleteByCustomerId(owner.customerId());
        } else {
            cartItemRepository.deleteBySessionId(owner.sessionId());
        }

        return toOrderMap(order);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> myOrders(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(this::toOrderMap).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));
        AuthUser user = SecurityUtils.currentUser()
                .orElseThrow(() -> new ApiException("Authentication required", HttpStatus.UNAUTHORIZED));
        boolean owner = user.isCustomer() && order.getCustomer() != null
                && order.getCustomer().getId().equals(user.getId());
        if (!owner && !user.isAdmin()) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }
        return toOrderMap(order);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listOrders(int page, int limit, OrderStatus status) {
        PageRequest pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.min(Math.max(limit, 1), 100),
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        Page<Order> result = status != null
                ? orderRepository.findByStatus(status, pageable)
                : orderRepository.findAll(pageable);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", result.getContent().stream().map(this::toAdminOrderMap).toList());
        response.put("page", page);
        response.put("limit", pageable.getPageSize());
        response.put("total", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAdminOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));
        return toAdminOrderMap(order);
    }

    @Transactional
    public Map<String, Object> updateStatus(Long id, StatusUpdateRequest req) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));
        order.setStatus(req.status());
        OrderHistory history = new OrderHistory();
        history.setOrder(order);
        history.setStatus(req.status());
        history.setComment(req.comment());
        history.setNotify(Boolean.TRUE.equals(req.notifyCustomer()));
        order.getHistory().add(history);
        return toOrderMap(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> validateCouponCode(String code) {
        Coupon coupon = validateCoupon(code, null);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("code", coupon.getCode());
        map.put("name", coupon.getName());
        map.put("type", coupon.getType());
        map.put("discount", coupon.getDiscount());
        map.put("minOrderTotal", coupon.getMinOrderTotal());
        return map;
    }

    private Coupon validateCoupon(String code, BigDecimal subtotal) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ApiException("Invalid coupon", HttpStatus.NOT_FOUND));
        if (!coupon.isActive()) {
            throw new ApiException("Invalid coupon", HttpStatus.BAD_REQUEST);
        }
        Instant now = Instant.now();
        if (coupon.getStartsAt() != null && coupon.getStartsAt().isAfter(now)) {
            throw new ApiException("Coupon is not active yet", HttpStatus.BAD_REQUEST);
        }
        if (coupon.getEndsAt() != null && coupon.getEndsAt().isBefore(now)) {
            throw new ApiException("Coupon has expired", HttpStatus.BAD_REQUEST);
        }
        if (coupon.getMaxUses() != null && coupon.getUsedCount() >= coupon.getMaxUses()) {
            throw new ApiException("Coupon usage limit reached", HttpStatus.BAD_REQUEST);
        }
        if (subtotal != null && coupon.getMinOrderTotal() != null
                && subtotal.compareTo(coupon.getMinOrderTotal()) < 0) {
            throw new ApiException("Order total too low for this coupon", HttpStatus.BAD_REQUEST);
        }
        return coupon;
    }

    private Map<String, Object> toOrderMap(Order order) {
        return toOrderMap(order, false);
    }

    public Map<String, Object> toAdminOrderMap(Order order) {
        return toOrderMap(order, true);
    }

    private Map<String, Object> toOrderMap(Order order, boolean admin) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", order.getId());
        map.put("orderNumber", order.getOrderNumber());
        map.put("status", order.getStatus().name());
        map.put("paymentStatus", order.getPaymentStatus().name());
        map.put("paymentMethod", order.getPaymentMethod());
        map.put("shippingMethod", order.getShippingMethod());
        map.put("email", order.getEmail());
        map.put("telephone", order.getTelephone());
        map.put("firstName", order.getFirstName());
        map.put("lastName", order.getLastName());
        map.put("subtotal", order.getSubtotal());
        map.put("shippingCost", order.getShippingCost());
        map.put("discountTotal", order.getDiscountTotal());
        map.put("taxTotal", order.getTaxTotal());
        map.put("total", order.getTotal());
        map.put("currency", order.getCurrency());
        map.put("comment", order.getComment());
        map.put("createdAt", order.getCreatedAt());
        map.put("items", order.getItems().stream().map(item -> {
            Map<String, Object> im = new LinkedHashMap<>();
            im.put("id", item.getId());
            im.put("name", item.getName());
            im.put("sku", item.getSku());
            im.put("quantity", item.getQuantity());
            im.put("price", item.getPrice());
            im.put("total", item.getTotal());
            im.put("options", item.getOptionsJson());
            return im;
        }).toList());
        map.put("history", order.getHistory().stream().map(h -> {
            Map<String, Object> hm = new LinkedHashMap<>();
            hm.put("id", h.getId());
            hm.put("status", h.getStatus().name());
            hm.put("comment", h.getComment());
            hm.put("createdAt", h.getCreatedAt());
            return hm;
        }).toList());
        if (admin) {
            map.put("customerId", order.getCustomer() != null ? order.getCustomer().getId() : null);
            map.put("shipping", Map.of(
                    "firstName", order.getShippingFirstName(),
                    "lastName", order.getShippingLastName(),
                    "company", order.getShippingCompany() != null ? order.getShippingCompany() : "",
                    "address1", order.getShippingAddress1(),
                    "address2", order.getShippingAddress2() != null ? order.getShippingAddress2() : "",
                    "city", order.getShippingCity(),
                    "postcode", order.getShippingPostcode(),
                    "country", order.getShippingCountry(),
                    "zone", order.getShippingZone() != null ? order.getShippingZone() : ""
            ));
            map.put("billing", Map.of(
                    "firstName", order.getBillingFirstName(),
                    "lastName", order.getBillingLastName(),
                    "company", order.getBillingCompany() != null ? order.getBillingCompany() : "",
                    "address1", order.getBillingAddress1(),
                    "address2", order.getBillingAddress2() != null ? order.getBillingAddress2() : "",
                    "city", order.getBillingCity(),
                    "postcode", order.getBillingPostcode(),
                    "country", order.getBillingCountry(),
                    "zone", order.getBillingZone() != null ? order.getBillingZone() : ""
            ));
        }
        return map;
    }
}
