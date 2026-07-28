package com.edison.scanner.model.market;

import java.math.BigDecimal;
import java.time.Instant;

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
public final class Candle {

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
    
    /**
     * Creates an immutable market candle.
     *
     * @param openTime opening time
     * @param open opening price
     * @param high highest price
     * @param low lowest price
     * @param close closing price
     * @param volume traded volume
     */
    public Candle(
            Instant openTime,
            BigDecimal open,
            BigDecimal high,
            BigDecimal low,
            BigDecimal close,
            BigDecimal volume,
            Instant closeTime) {

        this.openTime = openTime;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.closeTime = closeTime;
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

}