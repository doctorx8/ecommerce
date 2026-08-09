package com.ecommerce.store.repository;

import com.ecommerce.store.entity.Order;
import com.ecommerce.store.enums.OrderStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    Optional<Order> findByIdAndCustomerId(Long id, Long customerId);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
