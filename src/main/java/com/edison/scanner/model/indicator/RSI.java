package com.edison.scanner.model.indicator;

import com.edison.scanner.common.Timeframe;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents the Relative Strength Index (RSI) for a single candle.
 *
 * <p>
 * Each RSI value corresponds to a specific candle identified by its symbol,
 * timeframe and timestamps.
 */
public final class RSI {

    /**
     * Trading symbol.
     */
    private final String symbol;

    /**
     * Chart timeframe.
     */
    private final Timeframe timeframe;

    /**
     * Candle open time.
     */
    private final Instant openTime;

    /**
     * Candle close time.
     */
    private final Instant closeTime;

    /**
     * RSI value.
     */
    private final BigDecimal value;

    /**
     * Creates an RSI value.
     *
     * @param symbol
     *         trading symbol
     * @param timeframe
     *         chart timeframe
     * @param openTime
     *         candle open time
     * @param closeTime
     *         candle close time
     * @param value
     *         RSI value
     */
    public RSI(
            String symbol,
            Timeframe timeframe,
            Instant openTime,
            Instant closeTime,
            BigDecimal value) {

        this.symbol = Objects.requireNonNull(symbol, "symbol");
        this.timeframe = Objects.requireNonNull(timeframe, "timeframe");
        this.openTime = Objects.requireNonNull(openTime, "openTime");
        this.closeTime = Objects.requireNonNull(closeTime, "closeTime");
        this.value = Objects.requireNonNull(value, "value");

    }

    /**
     * Returns the trading symbol.
     *
     * @return trading symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the chart timeframe.
     *
     * @return timeframe
     */
    public Timeframe getTimeframe() {
        return timeframe;
    }

    /**
     * Returns the candle open time.
     *
     * @return open time
     */
    public Instant getOpenTime() {
        return openTime;
    }

    /**
     * Returns the candle close time.
     *
     * @return close time
     */
    public Instant getCloseTime() {
        return closeTime;
    }

    /**
     * Returns the RSI value.
     *
     * @return RSI value
     */
    public BigDecimal getValue() {
        return value;
    }

}