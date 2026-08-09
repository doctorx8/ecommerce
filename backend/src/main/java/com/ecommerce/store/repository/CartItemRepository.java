package com.ecommerce.store.repository;

import com.ecommerce.store.entity.CartItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCustomerIdOrderByCreatedAtAsc(Long customerId);
    List<CartItem> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    Optional<CartItem> findByIdAndCustomerId(Long id, Long customerId);
    Optional<CartItem> findByIdAndSessionId(Long id, String sessionId);
    Optional<CartItem> findByCustomerIdAndProductId(Long customerId, Long productId);
    Optional<CartItem> findBySessionIdAndProductId(String sessionId, Long productId);
    void deleteByCustomerId(Long customerId);
    void deleteBySessionId(String sessionId);
}
