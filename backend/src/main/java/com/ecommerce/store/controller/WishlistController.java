package com.ecommerce.store.controller;

import com.ecommerce.store.config.ApiException;
import com.ecommerce.store.security.AuthUser;
import com.ecommerce.store.service.WishlistService;
import com.ecommerce.store.util.SecurityUtils;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return wishlistService.list(currentCustomerId());
    }

    @PostMapping("/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> add(@PathVariable Long productId) {
        return wishlistService.add(currentCustomerId(), productId);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long productId) {
        wishlistService.remove(currentCustomerId(), productId);
    }

    private Long currentCustomerId() {
        AuthUser user = SecurityUtils.currentUser()
                .filter(AuthUser::isCustomer)
                .orElseThrow(() -> new ApiException("Customer access required", HttpStatus.FORBIDDEN));
        return user.getId();
    }
}
