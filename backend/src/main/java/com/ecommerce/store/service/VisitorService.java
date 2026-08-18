package com.ecommerce.store.service;

import com.ecommerce.store.entity.VisitorDailyStat;
import com.ecommerce.store.entity.VisitorSession;
import com.ecommerce.store.repository.VisitorDailyStatRepository;
import com.ecommerce.store.repository.VisitorSessionRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VisitorService {

    private final VisitorDailyStatRepository dailyStatRepository;
    private final VisitorSessionRepository sessionRepository;

    public VisitorService(
            VisitorDailyStatRepository dailyStatRepository, VisitorSessionRepository sessionRepository) {
        this.dailyStatRepository = dailyStatRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public Map<String, Object> recordVisit(String rawVisitorKey, String path) {
        String visitorKey = normalizeKey(rawVisitorKey);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        VisitorDailyStat day = dailyStatRepository
                .findByStatDate(today)
                .orElseGet(() -> {
                    VisitorDailyStat created = new VisitorDailyStat();
                    created.setStatDate(today);
                    return created;
                });

        day.setPageViews(day.getPageViews() + 1);

        boolean isNewVisitor = !sessionRepository.existsByVisitDateAndVisitorKey(today, visitorKey);
        if (isNewVisitor) {
            VisitorSession session = new VisitorSession();
            session.setVisitDate(today);
            session.setVisitorKey(visitorKey);
            session.setFirstSeenAt(Instant.now());
            sessionRepository.save(session);
            day.setUniqueVisitors(day.getUniqueVisitors() + 1);
        }

        dailyStatRepository.save(day);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("date", today.toString());
        res.put("pageViewsToday", day.getPageViews());
        res.put("uniqueVisitorsToday", day.getUniqueVisitors());
        res.put("newVisitor", isNewVisitor);
        res.put("path", path == null ? "" : path);
        return res;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> summary() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        VisitorDailyStat day = dailyStatRepository.findByStatDate(today).orElse(null);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("visitorsToday", day == null ? 0L : day.getUniqueVisitors());
        map.put("pageViewsToday", day == null ? 0L : day.getPageViews());
        map.put("visitorsTotal", dailyStatRepository.sumUniqueVisitors());
        map.put("pageViewsTotal", dailyStatRepository.sumPageViews());
        map.put("visitorsOverTime", visitorsOverTime(14));
        return map;
    }

    private List<Map<String, Object>> visitorsOverTime(int days) {
        LocalDate from = LocalDate.now(ZoneOffset.UTC).minusDays(days - 1L);
        Map<String, VisitorDailyStat> byDate = new LinkedHashMap<>();
        for (VisitorDailyStat row : dailyStatRepository.findByStatDateGreaterThanEqualOrderByStatDateAsc(from)) {
            byDate.put(row.getStatDate().toString(), row);
        }

        List<Map<String, Object>> series = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = from.plusDays(i);
            VisitorDailyStat row = byDate.get(d.toString());
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", d.toString());
            point.put("visitors", row == null ? 0L : row.getUniqueVisitors());
            point.put("pageViews", row == null ? 0L : row.getPageViews());
            series.add(point);
        }
        return series;
    }

    private static String normalizeKey(String raw) {
        if (raw == null || raw.isBlank()) {
            return "anon-" + Instant.now().toEpochMilli();
        }
        String trimmed = raw.trim();
        return trimmed.length() > 128 ? trimmed.substring(0, 128) : trimmed;
    }
}
