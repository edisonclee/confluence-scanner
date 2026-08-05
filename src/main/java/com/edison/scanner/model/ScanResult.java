package com.edison.scanner.model;

import java.util.Objects;

import com.edison.scanner.common.Timeframe;

/**
 * Represents a symbol that touched a Bollinger Band.
 */
public final class ScanResult {

    private final String symbol;

    private final Timeframe timeframe;

    public ScanResult(
            String symbol,
            Timeframe timeframe) {

        this.symbol = Objects.requireNonNull(symbol, "symbol");
        this.timeframe = Objects.requireNonNull(timeframe, "timeframe");

    }

    public String getSymbol() {
        return symbol;
    }

    public Timeframe getTimeframe() {
        return timeframe;
    }

}