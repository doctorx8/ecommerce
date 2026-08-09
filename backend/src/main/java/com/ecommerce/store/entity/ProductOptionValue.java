package com.ecommerce.store.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_option_values")
public class ProductOptionValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_option_id", nullable = false)
    private ProductOption productOption;

    @Column(nullable = false)
    private String name;

    @Column(name = "price_modifier", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceModifier = BigDecimal.ZERO;

    private Integer quantity;
    private String sku;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ProductOption getProductOption() { return productOption; }
    public void setProductOption(ProductOption productOption) { this.productOption = productOption; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPriceModifier() { return priceModifier; }
    public void setPriceModifier(BigDecimal priceModifier) { this.priceModifier = priceModifier; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
