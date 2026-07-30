package com.edison.scanner.model.market;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import com.edison.scanner.common.Timeframe;
import com.edison.scanner.model.CandleData;

/**
 * Represents a single immutable OHLCV market candle.
 *
 * <p>
 * This class is the application's domain model for market data.
 * It is intentionally independent of any exchange-specific response
 * format so that the application can switch exchanges without affecting
 * the trading logic.
 * </p>
 */
public final class Candle implements CandleData {

    /**
     * Candle opening time.
     */
    private final Instant openTime;

    /**
     * Opening price.
     */
    private final BigDecimal open;

    /**
     * Highest traded price.
     */
    private final BigDecimal high;

    /**
     * Lowest traded price.
     */
    private final BigDecimal low;

    /**
     * Closing price.
     */
    private final BigDecimal close;

    /**
     * Traded volume.
     */
    private final BigDecimal volume;

    /**
     * Candle opening time.
     */
    private final Instant closeTime;
    
    private final String symbol;
    
    private final Timeframe timeframe;
    
    /**
     * Creates a market candle.
     *
     * @param symbol trading symbol
     * @param timeframe candle timeframe
     * @param openTime candle open time
     * @param closeTime candle close time
     * @param open open price
     * @param high high price
     * @param low low price
     * @param close close price
     * @param volume traded volume
     */
    public Candle(
            String symbol,
            Timeframe timeframe,
            Instant openTime,
            Instant closeTime,
            BigDecimal open,
            BigDecimal high,
            BigDecimal low,
            BigDecimal close,
            BigDecimal volume) {

        this.symbol = Objects.requireNonNull(symbol, "symbol");
        this.timeframe = Objects.requireNonNull(timeframe, "timeframe");
        this.openTime = Objects.requireNonNull(openTime, "openTime");
        this.closeTime = Objects.requireNonNull(closeTime, "closeTime");
        this.open = Objects.requireNonNull(open, "open");
        this.high = Objects.requireNonNull(high, "high");
        this.low = Objects.requireNonNull(low, "low");
        this.close = Objects.requireNonNull(close, "close");
        this.volume = Objects.requireNonNull(volume, "volume");

    }

    /**
     * Returns the candle opening time.
     *
     * @return opening time
     */
    public Instant getOpenTime() {
        return openTime;
    }

    /**
     * Returns the opening price.
     *
     * @return opening price
     */
    public BigDecimal getOpen() {
        return open;
    }

    /**
     * Returns the highest traded price.
     *
     * @return highest price
     */
    public BigDecimal getHigh() {
        return high;
    }

    /**
     * Returns the lowest traded price.
     *
     * @return lowest price
     */
    public BigDecimal getLow() {
        return low;
    }

    /**
     * Returns the closing price.
     *
     * @return closing price
     */
    public BigDecimal getClose() {
        return close;
    }

    /**
     * Returns the traded volume.
     *
     * @return volume
     */
    public BigDecimal getVolume() {
        return volume;
    }

    @Override
    public String toString() {

        return "Candle{" +
                "openTime=" + openTime +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", volume=" + volume +
                ", closeTime=" + closeTime +
                '}';
    }

	public Instant getCloseTime() {
		return closeTime;
	}

	public String getSymbol() {
		return symbol;
	}

	public Timeframe getTimeframe() {
		return timeframe;
	}

}