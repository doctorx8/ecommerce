package com.ecommerce.store.repository;

import com.ecommerce.store.entity.VisitorSession;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitorSessionRepository extends JpaRepository<VisitorSession, Long> {
    boolean existsByVisitDateAndVisitorKey(LocalDate visitDate, String visitorKey);
}
