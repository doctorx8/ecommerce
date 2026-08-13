package com.ecommerce.store.service;

import com.ecommerce.store.config.ApiException;
import com.ecommerce.store.enums.OrderStatus;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusRules {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.of(
            OrderStatus.PENDING, EnumSet.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
            OrderStatus.PROCESSING, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED, OrderStatus.REFUNDED),
            OrderStatus.SHIPPED, EnumSet.of(OrderStatus.DELIVERED, OrderStatus.REFUNDED),
            OrderStatus.DELIVERED, EnumSet.of(OrderStatus.REFUNDED),
            OrderStatus.CANCELLED, EnumSet.of(OrderStatus.REFUNDED),
            OrderStatus.REFUNDED, EnumSet.noneOf(OrderStatus.class)
    );

    public void assertTransition(OrderStatus from, OrderStatus to) {
        if (from == to) {
            return;
        }
        Set<OrderStatus> allowed = ALLOWED.getOrDefault(from, EnumSet.noneOf(OrderStatus.class));
        if (!allowed.contains(to)) {
            throw new ApiException(
                    "Invalid status transition: " + from + " → " + to,
                    HttpStatus.BAD_REQUEST);
        }
    }

    public boolean canCancel(OrderStatus status) {
        return status == OrderStatus.PENDING || status == OrderStatus.PROCESSING;
    }

    public boolean canRefund(OrderStatus status) {
        return status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED || status == OrderStatus.PROCESSING;
    }
}
