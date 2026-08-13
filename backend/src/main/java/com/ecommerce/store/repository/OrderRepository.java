package com.ecommerce.store.repository;

import com.ecommerce.store.entity.Order;
import com.ecommerce.store.enums.OrderStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    Optional<Order> findByIdAndCustomerId(Long id, Long customerId);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    long countByStatus(OrderStatus status);
    long countByCustomerId(Long customerId);

    @Query("select coalesce(sum(o.total), 0) from Order o where o.status not in (com.ecommerce.store.enums.OrderStatus.CANCELLED, com.ecommerce.store.enums.OrderStatus.REFUNDED)")
    BigDecimal sumRevenueExcludingCancelled();

    @Query(value = """
            select date(o.created_at) as day, coalesce(sum(o.total), 0) as revenue, count(*) as orders
            from orders o
            where o.created_at >= (current_date - interval 13 day)
              and o.status not in ('CANCELLED', 'REFUNDED')
            group by date(o.created_at)
            order by day asc
            """, nativeQuery = true)
    List<Object[]> salesLast14Days();
}
