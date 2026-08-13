package com.ecommerce.store.entity;

import com.ecommerce.store.enums.OrderStatus;
import com.ecommerce.store.enums.PaymentStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_ref")
    private String paymentRef;

    @Column(name = "shipping_method")
    private String shippingMethod;

    @Column(nullable = false)
    private String email;

    private String telephone;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "shipping_first_name", nullable = false)
    private String shippingFirstName;
    @Column(name = "shipping_last_name", nullable = false)
    private String shippingLastName;
    @Column(name = "shipping_company")
    private String shippingCompany;
    @Column(name = "shipping_address1", nullable = false)
    private String shippingAddress1;
    @Column(name = "shipping_address2")
    private String shippingAddress2;
    @Column(name = "shipping_city", nullable = false)
    private String shippingCity;
    @Column(name = "shipping_postcode", nullable = false)
    private String shippingPostcode;
    @Column(name = "shipping_country", nullable = false)
    private String shippingCountry;
    @Column(name = "shipping_zone")
    private String shippingZone;

    @Column(name = "billing_first_name", nullable = false)
    private String billingFirstName;
    @Column(name = "billing_last_name", nullable = false)
    private String billingLastName;
    @Column(name = "billing_company")
    private String billingCompany;
    @Column(name = "billing_address1", nullable = false)
    private String billingAddress1;
    @Column(name = "billing_address2")
    private String billingAddress2;
    @Column(name = "billing_city", nullable = false)
    private String billingCity;
    @Column(name = "billing_postcode", nullable = false)
    private String billingPostcode;
    @Column(name = "billing_country", nullable = false)
    private String billingCountry;
    @Column(name = "billing_zone")
    private String billingZone;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "shipping_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(name = "discount_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountTotal = BigDecimal.ZERO;

    @Column(name = "tax_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(nullable = false)
    private String currency = "USD";

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "ip_address")
    private String ipAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<OrderHistory> history = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public Coupon getCoupon() { return coupon; }
    public void setCoupon(Coupon coupon) { this.coupon = coupon; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentRef() { return paymentRef; }
    public void setPaymentRef(String paymentRef) { this.paymentRef = paymentRef; }
    public String getShippingMethod() { return shippingMethod; }
    public void setShippingMethod(String shippingMethod) { this.shippingMethod = shippingMethod; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getShippingFirstName() { return shippingFirstName; }
    public void setShippingFirstName(String shippingFirstName) { this.shippingFirstName = shippingFirstName; }
    public String getShippingLastName() { return shippingLastName; }
    public void setShippingLastName(String shippingLastName) { this.shippingLastName = shippingLastName; }
    public String getShippingCompany() { return shippingCompany; }
    public void setShippingCompany(String shippingCompany) { this.shippingCompany = shippingCompany; }
    public String getShippingAddress1() { return shippingAddress1; }
    public void setShippingAddress1(String shippingAddress1) { this.shippingAddress1 = shippingAddress1; }
    public String getShippingAddress2() { return shippingAddress2; }
    public void setShippingAddress2(String shippingAddress2) { this.shippingAddress2 = shippingAddress2; }
    public String getShippingCity() { return shippingCity; }
    public void setShippingCity(String shippingCity) { this.shippingCity = shippingCity; }
    public String getShippingPostcode() { return shippingPostcode; }
    public void setShippingPostcode(String shippingPostcode) { this.shippingPostcode = shippingPostcode; }
    public String getShippingCountry() { return shippingCountry; }
    public void setShippingCountry(String shippingCountry) { this.shippingCountry = shippingCountry; }
    public String getShippingZone() { return shippingZone; }
    public void setShippingZone(String shippingZone) { this.shippingZone = shippingZone; }
    public String getBillingFirstName() { return billingFirstName; }
    public void setBillingFirstName(String billingFirstName) { this.billingFirstName = billingFirstName; }
    public String getBillingLastName() { return billingLastName; }
    public void setBillingLastName(String billingLastName) { this.billingLastName = billingLastName; }
    public String getBillingCompany() { return billingCompany; }
    public void setBillingCompany(String billingCompany) { this.billingCompany = billingCompany; }
    public String getBillingAddress1() { return billingAddress1; }
    public void setBillingAddress1(String billingAddress1) { this.billingAddress1 = billingAddress1; }
    public String getBillingAddress2() { return billingAddress2; }
    public void setBillingAddress2(String billingAddress2) { this.billingAddress2 = billingAddress2; }
    public String getBillingCity() { return billingCity; }
    public void setBillingCity(String billingCity) { this.billingCity = billingCity; }
    public String getBillingPostcode() { return billingPostcode; }
    public void setBillingPostcode(String billingPostcode) { this.billingPostcode = billingPostcode; }
    public String getBillingCountry() { return billingCountry; }
    public void setBillingCountry(String billingCountry) { this.billingCountry = billingCountry; }
    public String getBillingZone() { return billingZone; }
    public void setBillingZone(String billingZone) { this.billingZone = billingZone; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getShippingCost() { return shippingCost; }
    public void setShippingCost(BigDecimal shippingCost) { this.shippingCost = shippingCost; }
    public BigDecimal getDiscountTotal() { return discountTotal; }
    public void setDiscountTotal(BigDecimal discountTotal) { this.discountTotal = discountTotal; }
    public BigDecimal getTaxTotal() { return taxTotal; }
    public void setTaxTotal(BigDecimal taxTotal) { this.taxTotal = taxTotal; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public List<OrderItem> getItems() { return items; }
    public List<OrderHistory> getHistory() { return history; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
