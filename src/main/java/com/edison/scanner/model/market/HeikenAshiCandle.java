package com.edison.scanner.model.market;

import com.edison.scanner.common.Timeframe;
import com.edison.scanner.model.CandleData;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a Heiken Ashi candle.
 *
 * <p>
 * A Heiken Ashi candle is derived from a market candle and exposes the
 * same API as a standard candle, allowing indicator calculators to work
 * with either implementation transparently.
 * </p>
 */
public final class HeikenAshiCandle implements CandleData {

    private final String symbol;
    private final Timeframe timeframe;
    private final Instant openTime;
    private final Instant closeTime;
    private final BigDecimal open;
    private final BigDecimal high;
    private final BigDecimal low;
    private final BigDecimal close;
    private final BigDecimal volume;

    public HeikenAshiCandle(
            String symbol,
            Timeframe timeframe,
            Instant openTime,
            Instant closeTime,
            BigDecimal open,
            BigDecimal high,
            BigDecimal low,
            BigDecimal close,
            BigDecimal volume) {

        this.symbol = Objects.requireNonNull(symbol);
        this.timeframe = Objects.requireNonNull(timeframe);
        this.openTime = Objects.requireNonNull(openTime);
        this.closeTime = Objects.requireNonNull(closeTime);
        this.open = Objects.requireNonNull(open);
        this.high = Objects.requireNonNull(high);
        this.low = Objects.requireNonNull(low);
        this.close = Objects.requireNonNull(close);
        this.volume = Objects.requireNonNull(volume);

    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public Timeframe getTimeframe() {
        return timeframe;
    }

    @Override
    public Instant getOpenTime() {
        return openTime;
    }

    @Override
    public Instant getCloseTime() {
        return closeTime;
    }

    @Override
    public BigDecimal getOpen() {
        return open;
    }

    @Override
    public BigDecimal getHigh() {
        return high;
    }

    @Override
    public BigDecimal getLow() {
        return low;
    }

    @Override
    public BigDecimal getClose() {
        return close;
    }

    @Override
    public BigDecimal getVolume() {
        return volume;
    }

}