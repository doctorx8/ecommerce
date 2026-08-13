package com.ecommerce.store.service;

import com.ecommerce.store.config.ApiException;
import com.ecommerce.store.entity.Customer;
import com.ecommerce.store.entity.Product;
import com.ecommerce.store.entity.WishlistItem;
import com.ecommerce.store.repository.CustomerRepository;
import com.ecommerce.store.repository.ProductRepository;
import com.ecommerce.store.repository.WishlistRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public WishlistService(
            WishlistRepository wishlistRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(Long customerId) {
        return wishlistRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(this::toMap).toList();
    }

    @Transactional
    public Map<String, Object> add(Long customerId, Long productId) {
        if (wishlistRepository.existsByCustomerIdAndProductId(customerId, productId)) {
            return toMap(wishlistRepository.findByCustomerIdAndProductId(customerId, productId).orElseThrow());
        }
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ApiException("Customer not found", HttpStatus.NOT_FOUND));
        Product product = productRepository.findByIdAndActiveTrue(productId)
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));
        WishlistItem item = new WishlistItem();
        item.setCustomer(customer);
        item.setProduct(product);
        return toMap(wishlistRepository.save(item));
    }

    @Transactional
    public void remove(Long customerId, Long productId) {
        wishlistRepository.deleteByCustomerIdAndProductId(customerId, productId);
    }

    private Map<String, Object> toMap(WishlistItem item) {
        Product p = item.getProduct();
        Map<String, Object> product = new LinkedHashMap<>();
        product.put("id", p.getId());
        product.put("name", p.getName());
        product.put("slug", p.getSlug());
        product.put("sku", p.getSku());
        product.put("price", p.getPrice());
        product.put("stockStatus", p.getStockStatus().name());
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", item.getId());
        map.put("createdAt", item.getCreatedAt());
        map.put("product", product);
        return map;
    }
}
