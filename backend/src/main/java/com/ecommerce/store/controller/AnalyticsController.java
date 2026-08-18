package com.ecommerce.store.controller;

import com.ecommerce.store.service.VisitorService;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final VisitorService visitorService;

    public AnalyticsController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    public record VisitRequest(String visitorKey, String path) {}

    @PostMapping("/visit")
    public Map<String, Object> visit(@RequestBody(required = false) VisitRequest body) {
        String key = body == null ? null : body.visitorKey();
        String path = body == null ? null : body.path();
        return visitorService.recordVisit(key, path);
    }
}
