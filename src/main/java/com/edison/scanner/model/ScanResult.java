package com.edison.scanner.model;

import java.math.BigDecimal;
import java.util.Objects;

import com.edison.scanner.common.Timeframe;

/**
 * Scanner result.
 */
public final class ScanResult {

    /**
     * Trading symbol.
     */
    private final String symbol;

    /**
     * Chart timeframe.
     */
    private final Timeframe timeframe;

    /**
     * Bollinger Band width in percent.
     */
    private final BigDecimal bbWidthPercent;

    public ScanResult(
            String symbol,
            Timeframe timeframe,
            BigDecimal bbWidthPercent) {

        this.symbol =
                Objects.requireNonNull(symbol);

        this.timeframe =
                Objects.requireNonNull(timeframe);

        this.bbWidthPercent =
                Objects.requireNonNull(bbWidthPercent);

    }

    public String getSymbol() {
        return symbol;
    }

    public Timeframe getTimeframe() {
        return timeframe;
    }

    public BigDecimal getBbWidthPercent() {
        return bbWidthPercent;
    }

}