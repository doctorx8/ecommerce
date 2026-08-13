package com.ecommerce.store.service;

import com.ecommerce.store.config.ApiException;
import com.ecommerce.store.dto.ReviewDtos.CreateReviewRequest;
import com.ecommerce.store.entity.Customer;
import com.ecommerce.store.entity.Product;
import com.ecommerce.store.entity.Review;
import com.ecommerce.store.repository.CustomerRepository;
import com.ecommerce.store.repository.ProductRepository;
import com.ecommerce.store.repository.ReviewRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final AuditService auditService;

    public ReviewService(
            ReviewRepository reviewRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            AuditService auditService) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listApproved(Long productId) {
        return reviewRepository.findByProductIdAndApprovedTrueOrderByCreatedAtDesc(productId)
                .stream().map(this::toMap).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listPending() {
        return reviewRepository.findByApprovedFalseOrderByCreatedAtDesc()
                .stream().map(this::toMap).toList();
    }

    @Transactional
    public Map<String, Object> create(Long customerId, CreateReviewRequest req) {
        Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));
        if (reviewRepository.existsByProductIdAndCustomerId(product.getId(), customerId)) {
            throw new ApiException("You already reviewed this product", HttpStatus.CONFLICT);
        }
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ApiException("Customer not found", HttpStatus.NOT_FOUND));
        Review review = new Review();
        review.setProduct(product);
        review.setCustomer(customer);
        review.setAuthor(req.author() != null && !req.author().isBlank()
                ? req.author()
                : customer.getFirstName() + " " + customer.getLastName());
        review.setRating(req.rating());
        review.setText(req.text());
        review.setApproved(false);
        return toMap(reviewRepository.save(review));
    }

    @Transactional
    public Map<String, Object> approve(Long id, boolean approved) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ApiException("Review not found", HttpStatus.NOT_FOUND));
        review.setApproved(approved);
        Review saved = reviewRepository.save(review);
        auditService.record(
                approved ? "REVIEW_APPROVE" : "REVIEW_REJECT",
                "Review",
                id,
                "productId=" + saved.getProduct().getId());
        return toMap(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ApiException("Review not found", HttpStatus.NOT_FOUND);
        }
        reviewRepository.deleteById(id);
        auditService.record("REVIEW_DELETE", "Review", id, null);
    }

    private Map<String, Object> toMap(Review r) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", r.getId());
        map.put("productId", r.getProduct().getId());
        map.put("customerId", r.getCustomer() != null ? r.getCustomer().getId() : null);
        map.put("author", r.getAuthor());
        map.put("rating", r.getRating());
        map.put("text", r.getText());
        map.put("approved", r.isApproved());
        map.put("createdAt", r.getCreatedAt());
        return map;
    }
}
