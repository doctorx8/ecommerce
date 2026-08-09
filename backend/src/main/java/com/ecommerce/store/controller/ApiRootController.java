package com.ecommerce.store.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiRootController {

    @GetMapping({"", "/"})
    public Map<String, Object> root() {
        Map<String, Object> docs = new LinkedHashMap<>();
        docs.put("health", "GET /api/health");
        docs.put("products", "GET /api/products");
        docs.put("categories", "GET /api/categories?tree=true");
        docs.put("manufacturers", "GET /api/manufacturers");
        docs.put("authLogin", "POST /api/auth/login");
        docs.put("authRegister", "POST /api/auth/register");
        docs.put("cart", "GET /api/cart?sessionId=...");
        docs.put("checkout", "POST /api/orders/checkout");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "ok");
        body.put("service", "ecommerce-api");
        body.put("stack", "Java Spring Boot + MySQL");
        body.put("docs", docs);
        return body;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok", "service", "ecommerce-api");
    }
}
