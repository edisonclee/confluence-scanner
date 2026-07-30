package com.edison.scanner.model;

import com.edison.scanner.common.BandType;
import com.edison.scanner.common.Timeframe;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a Bollinger Band touch detected on a candle.
 */
public final class TouchResult {

    private final String symbol;

    private final Timeframe timeframe;

    private final Instant openTime;

    private final Instant closeTime;

    private final BandType bandType;

    public TouchResult(
            String symbol,
            Timeframe timeframe,
            Instant openTime,
            Instant closeTime,
            BandType bandType) {

        this.symbol = Objects.requireNonNull(symbol, "symbol");
        this.timeframe = Objects.requireNonNull(timeframe, "timeframe");
        this.openTime = Objects.requireNonNull(openTime, "openTime");
        this.closeTime = Objects.requireNonNull(closeTime, "closeTime");
        this.bandType = Objects.requireNonNull(bandType, "bandType");

    }

    public String getSymbol() {
        return symbol;
    }

    public Timeframe getTimeframe() {
        return timeframe;
    }

    public Instant getOpenTime() {
        return openTime;
    }

    public Instant getCloseTime() {
        return closeTime;
    }

    public BandType getBandType() {
        return bandType;
    }

    @Override
    public String toString() {

        return "TouchResult{" +
                "symbol='" + symbol + '\'' +
                ", timeframe=" + timeframe +
                ", bandType=" + bandType +
                ", closeTime=" + closeTime +
                '}';

    }

}