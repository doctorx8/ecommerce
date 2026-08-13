package com.ecommerce.store.controller;

import com.ecommerce.store.config.ApiException;
import com.ecommerce.store.dto.ReviewDtos.CreateReviewRequest;
import com.ecommerce.store.security.AuthUser;
import com.ecommerce.store.service.ReviewService;
import com.ecommerce.store.util.SecurityUtils;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/product/{productId}")
    public List<Map<String, Object>> forProduct(@PathVariable Long productId) {
        return reviewService.listApproved(productId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@Valid @RequestBody CreateReviewRequest request) {
        AuthUser user = SecurityUtils.currentUser()
                .filter(AuthUser::isCustomer)
                .orElseThrow(() -> new ApiException("Customer access required", HttpStatus.FORBIDDEN));
        return reviewService.create(user.getId(), request);
    }
}
