package com.edison.scanner.model.indicator;

import com.edison.scanner.common.Timeframe;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents the Bollinger Bands calculated for a single candle.
 *
 * <p>
 * This class is an immutable value object containing the calculated
 * Bollinger Band values and the metadata required to identify the
 * corresponding candle.
 * </p>
 */
public final class BollingerBand {

    /**
     * Trading symbol.
     */
    private final String symbol;

    /**
     * Candle timeframe.
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
     * Upper Bollinger Band.
     */
    private final BigDecimal upperBand;

    /**
     * Middle Bollinger Band (SMA).
     */
    private final BigDecimal basisBand;

    /**
     * Lower Bollinger Band.
     */
    private final BigDecimal lowerBand;

    /**
     * Creates a Bollinger Band.
     *
     * @param symbol trading symbol
     * @param timeframe candle timeframe
     * @param openTime candle open time
     * @param closeTime candle close time
     * @param upperBand upper Bollinger Band
     * @param basisBand middle Bollinger Band
     * @param lowerBand lower Bollinger Band
     */
    public BollingerBand(
            String symbol,
            Timeframe timeframe,
            Instant openTime,
            Instant closeTime,
            BigDecimal upperBand,
            BigDecimal basisBand,
            BigDecimal lowerBand) {

        this.symbol = Objects.requireNonNull(symbol, "symbol");
        this.timeframe = Objects.requireNonNull(timeframe, "timeframe");
        this.openTime = Objects.requireNonNull(openTime, "openTime");
        this.closeTime = Objects.requireNonNull(closeTime, "closeTime");
        this.upperBand = Objects.requireNonNull(upperBand, "upperBand");
        this.basisBand = Objects.requireNonNull(basisBand, "basisBand");
        this.lowerBand = Objects.requireNonNull(lowerBand, "lowerBand");

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

    public BigDecimal getUpperBand() {
        return upperBand;
    }

    public BigDecimal getBasisBand() {
        return basisBand;
    }

    public BigDecimal getLowerBand() {
        return lowerBand;
    }

    @Override
    public String toString() {
        return "BollingerBand{" +
                "symbol='" + symbol + '\'' +
                ", timeframe=" + timeframe +
                ", closeTime=" + closeTime +
                ", upperBand=" + upperBand +
                ", basisBand=" + basisBand +
                ", lowerBand=" + lowerBand +
                '}';
    }

}