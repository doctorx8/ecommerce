package com.ecommerce.store.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
        name = "visitor_daily_stats",
        uniqueConstraints = @UniqueConstraint(name = "uk_visitor_daily_date", columnNames = "stat_date"))
public class VisitorDailyStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "page_views", nullable = false)
    private long pageViews = 0;

    @Column(name = "unique_visitors", nullable = false)
    private long uniqueVisitors = 0;

    public Long getId() {
        return id;
    }

    public LocalDate getStatDate() {
        return statDate;
    }

    public void setStatDate(LocalDate statDate) {
        this.statDate = statDate;
    }

    public long getPageViews() {
        return pageViews;
    }

    public void setPageViews(long pageViews) {
        this.pageViews = pageViews;
    }

    public long getUniqueVisitors() {
        return uniqueVisitors;
    }

    public void setUniqueVisitors(long uniqueVisitors) {
        this.uniqueVisitors = uniqueVisitors;
    }
}
