package com.ecommerce.store.controller;

import com.ecommerce.store.dto.CartDtos.AddItemRequest;
import com.ecommerce.store.dto.CartDtos.UpdateItemRequest;
import com.ecommerce.store.service.CartService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Map<String, Object> getCart(@RequestParam(required = false) String sessionId) {
        return cartService.getCart(sessionId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> add(@Valid @RequestBody AddItemRequest request) {
        return cartService.addItem(request);
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateItemRequest request,
            @RequestParam(required = false) String sessionId) {
        return cartService.updateItem(id, request.quantity(), sessionId);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> remove(
            @PathVariable Long id,
            @RequestParam(required = false) String sessionId) {
        return cartService.removeItem(id, sessionId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@RequestParam(required = false) String sessionId) {
        cartService.clear(sessionId);
    }
}
