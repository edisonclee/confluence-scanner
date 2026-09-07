package com.edison.scanner.oi.model;

import java.math.BigDecimal;

public record OiRadarResult(
        String symbol,
        BigDecimal price,
        BigDecimal marketCap,
        BigDecimal volume1h,
        BigDecimal volume4h,
        BigDecimal openInterest,
        BigDecimal openInterestChange1h,
        BigDecimal openInterestChange4h,
        BigDecimal fundingRate,
        BigDecimal priceChange1h,
        BigDecimal priceChange4h,
        int radarScore,
        String status) {
}