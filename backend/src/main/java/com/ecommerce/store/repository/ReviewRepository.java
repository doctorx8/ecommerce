package com.ecommerce.store.repository;

import com.ecommerce.store.entity.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductIdAndApprovedTrueOrderByCreatedAtDesc(Long productId);
    List<Review> findByApprovedFalseOrderByCreatedAtDesc();
    boolean existsByProductIdAndCustomerId(Long productId, Long customerId);
}
