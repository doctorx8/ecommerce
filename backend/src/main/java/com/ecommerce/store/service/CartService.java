package com.ecommerce.store.service;

import com.ecommerce.store.config.ApiException;
import com.ecommerce.store.dto.CartDtos.AddItemRequest;
import com.ecommerce.store.entity.CartItem;
import com.ecommerce.store.entity.Customer;
import com.ecommerce.store.entity.Product;
import com.ecommerce.store.repository.CartItemRepository;
import com.ecommerce.store.repository.CustomerRepository;
import com.ecommerce.store.repository.ProductRepository;
import com.ecommerce.store.security.AuthUser;
import com.ecommerce.store.util.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final ObjectMapper objectMapper;

    public CartService(
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            ObjectMapper objectMapper) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCart(String sessionId) {
        return summarize(loadItems(resolveOwner(sessionId)));
    }

    @Transactional
    public Map<String, Object> addItem(AddItemRequest req) {
        Owner owner = resolveOwner(req.sessionId());
        Product product = productRepository.findByIdAndActiveTrue(req.productId())
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));
        if (product.isSubtractStock() && product.getQuantity() < req.quantity()) {
            throw new ApiException("Insufficient stock", HttpStatus.BAD_REQUEST);
        }

        CartItem item = owner.customerId() != null
                ? cartItemRepository.findByCustomerIdAndProductId(owner.customerId(), product.getId()).orElse(null)
                : cartItemRepository.findBySessionIdAndProductId(owner.sessionId(), product.getId()).orElse(null);

        if (item == null) {
            item = new CartItem();
            item.setProduct(product);
            if (owner.customerId() != null) {
                Customer customer = customerRepository.findById(owner.customerId())
                        .orElseThrow(() -> new ApiException("Customer not found", HttpStatus.NOT_FOUND));
                item.setCustomer(customer);
            } else {
                item.setSessionId(owner.sessionId());
            }
            item.setQuantity(req.quantity());
        } else {
            item.setQuantity(item.getQuantity() + req.quantity());
        }

        if (req.options() != null) {
            try {
                item.setOptionsJson(objectMapper.writeValueAsString(req.options()));
            } catch (JsonProcessingException e) {
                throw new ApiException("Invalid options", HttpStatus.BAD_REQUEST);
            }
        }

        cartItemRepository.save(item);
        return summarize(loadItems(owner));
    }

    @Transactional
    public Map<String, Object> updateItem(Long id, int quantity, String sessionId) {
        Owner owner = resolveOwner(sessionId);
        CartItem item = findOwnedItem(id, owner);
        if (item.getProduct().isSubtractStock() && item.getProduct().getQuantity() < quantity) {
            throw new ApiException("Insufficient stock", HttpStatus.BAD_REQUEST);
        }
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return summarize(loadItems(owner));
    }

    @Transactional
    public Map<String, Object> removeItem(Long id, String sessionId) {
        Owner owner = resolveOwner(sessionId);
        CartItem item = findOwnedItem(id, owner);
        cartItemRepository.delete(item);
        return summarize(loadItems(owner));
    }

    @Transactional
    public void clear(String sessionId) {
        Owner owner = resolveOwner(sessionId);
        if (owner.customerId() != null) {
            cartItemRepository.deleteByCustomerId(owner.customerId());
        } else {
            cartItemRepository.deleteBySessionId(owner.sessionId());
        }
    }

    public List<CartItem> loadItems(Owner owner) {
        if (owner.customerId() != null) {
            return cartItemRepository.findByCustomerIdOrderByCreatedAtAsc(owner.customerId());
        }
        return cartItemRepository.findBySessionIdOrderByCreatedAtAsc(owner.sessionId());
    }

    public Owner resolveOwner(String sessionId) {
        AuthUser user = SecurityUtils.currentUser().orElse(null);
        if (user != null && user.isCustomer()) {
            return new Owner(user.getId(), null);
        }
        if (sessionId == null || sessionId.isBlank()) {
            throw new ApiException("sessionId is required for guest carts", HttpStatus.BAD_REQUEST);
        }
        return new Owner(null, sessionId);
    }

    private CartItem findOwnedItem(Long id, Owner owner) {
        return (owner.customerId() != null
                ? cartItemRepository.findByIdAndCustomerId(id, owner.customerId())
                : cartItemRepository.findByIdAndSessionId(id, owner.sessionId()))
                .orElseThrow(() -> new ApiException("Cart item not found", HttpStatus.NOT_FOUND));
    }

    private Map<String, Object> summarize(List<CartItem> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        int count = 0;
        List<Map<String, Object>> mapped = new java.util.ArrayList<>();
        for (CartItem item : items) {
            BigDecimal line = item.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(line);
            count += item.getQuantity();

            Map<String, Object> product = new LinkedHashMap<>();
            product.put("id", item.getProduct().getId());
            product.put("name", item.getProduct().getName());
            product.put("slug", item.getProduct().getSlug());
            product.put("sku", item.getProduct().getSku());
            product.put("price", item.getProduct().getPrice());
            product.put("quantity", item.getProduct().getQuantity());
            product.put("images", item.getProduct().getImages().stream().limit(1).map(img -> Map.of(
                    "id", img.getId(),
                    "image", img.getImage(),
                    "alt", img.getAlt() == null ? "" : img.getAlt()
            )).toList());

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("quantity", item.getQuantity());
            row.put("options", item.getOptionsJson());
            row.put("product", product);
            mapped.add(row);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", mapped);
        response.put("itemCount", count);
        response.put("subtotal", subtotal.setScale(2, RoundingMode.HALF_UP));
        return response;
    }

    public record Owner(Long customerId, String sessionId) {}
}
