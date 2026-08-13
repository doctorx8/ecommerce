package com.ecommerce.store.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

    private final BigDecimal flatRate;
    private final BigDecimal freeThreshold;
    private final BigDecimal taxRate;

    public PricingService(
            @Value("${app.shipping.flat-rate:9.99}") BigDecimal flatRate,
            @Value("${app.shipping.free-threshold:100}") BigDecimal freeThreshold,
            @Value("${app.tax.rate:0.08}") BigDecimal taxRate) {
        this.flatRate = flatRate;
        this.freeThreshold = freeThreshold;
        this.taxRate = taxRate;
    }

    public BigDecimal shippingFor(BigDecimal taxableSubtotalAfterDiscount) {
        if (taxableSubtotalAfterDiscount.compareTo(freeThreshold) >= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return flatRate.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal taxFor(BigDecimal amountAfterDiscount) {
        if (amountAfterDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return amountAfterDiscount.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
    }

    public Map<String, Object> quote(BigDecimal subtotal, BigDecimal discount) {
        BigDecimal safeSubtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        BigDecimal safeDiscount = discount != null ? discount : BigDecimal.ZERO;
        if (safeDiscount.compareTo(safeSubtotal) > 0) {
            safeDiscount = safeSubtotal;
        }
        BigDecimal afterDiscount = safeSubtotal.subtract(safeDiscount);
        BigDecimal shipping = shippingFor(afterDiscount);
        BigDecimal tax = taxFor(afterDiscount);
        BigDecimal total = afterDiscount.add(shipping).add(tax);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("subtotal", safeSubtotal.setScale(2, RoundingMode.HALF_UP));
        map.put("discountTotal", safeDiscount.setScale(2, RoundingMode.HALF_UP));
        map.put("shippingCost", shipping);
        map.put("taxTotal", tax);
        map.put("taxRate", taxRate);
        map.put("shippingFlatRate", flatRate);
        map.put("shippingFreeThreshold", freeThreshold);
        map.put("total", total.setScale(2, RoundingMode.HALF_UP));
        return map;
    }
}
