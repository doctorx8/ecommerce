package com.ecommerce.store.repository;

import com.ecommerce.store.entity.VisitorDailyStat;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VisitorDailyStatRepository extends JpaRepository<VisitorDailyStat, Long> {
    Optional<VisitorDailyStat> findByStatDate(LocalDate statDate);

    List<VisitorDailyStat> findByStatDateGreaterThanEqualOrderByStatDateAsc(LocalDate from);

    @Query("select coalesce(sum(v.pageViews), 0) from VisitorDailyStat v")
    long sumPageViews();

    @Query("select coalesce(sum(v.uniqueVisitors), 0) from VisitorDailyStat v")
    long sumUniqueVisitors();
}
