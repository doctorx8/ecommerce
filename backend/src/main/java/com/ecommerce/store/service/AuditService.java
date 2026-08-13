package com.ecommerce.store.service;

import com.ecommerce.store.entity.AuditLog;
import com.ecommerce.store.repository.AuditLogRepository;
import com.ecommerce.store.security.AuthUser;
import com.ecommerce.store.util.SecurityUtils;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(String action, String entityType, Object entityId, String details) {
        AuditLog log = new AuditLog();
        SecurityUtils.currentUser().ifPresent(user -> {
            log.setActorId(user.getId());
            log.setActorEmail(user.getUsername());
            log.setActorRole(user.isAdmin() ? user.getRole() : user.getType());
        });
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId != null ? String.valueOf(entityId) : null);
        log.setDetails(details);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> list(int page, int limit) {
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), Math.min(Math.max(limit, 1), 100));
        Page<AuditLog> result = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", result.getContent().stream().map(this::toMap).toList());
        response.put("page", page);
        response.put("limit", pageable.getPageSize());
        response.put("total", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        return response;
    }

    private Map<String, Object> toMap(AuditLog log) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", log.getId());
        map.put("actorId", log.getActorId());
        map.put("actorEmail", log.getActorEmail());
        map.put("actorRole", log.getActorRole());
        map.put("action", log.getAction());
        map.put("entityType", log.getEntityType());
        map.put("entityId", log.getEntityId());
        map.put("details", log.getDetails());
        map.put("createdAt", log.getCreatedAt());
        return map;
    }
}
