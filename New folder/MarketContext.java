package com.edison.scanner.model;

import com.edison.scanner.model.indicator.BollingerBand;
import java.util.List;
import java.util.Objects;

/**
 * Represents the complete market state for a single symbol and timeframe.
 *
 * <p>
 * A {@code MarketContext} is an immutable snapshot that contains all market
 * data, calculated indicators, and detection results required for evaluating
 * trading rules.
 *
 * <p>
 * As additional indicators are introduced (for example RSI, MACD, EMA and ATR),
 * this class can be extended with new fields without affecting existing rules.
 */
public final class MarketContext {

    /**
     * Current candle.
     */
    private final CandleData candle;

    /**
     * Bollinger Band values.
     */
    private final BollingerBand bollingerBand;

    /**
     * Bollinger Band touch results.
     */
    private final List<TouchResult> touchResults;

    /**
     * Creates a market context.
     *
     * @param candle
     *         current candle
     * @param bollingerBand
     *         Bollinger Band values
     * @param touchResults
     *         detected Bollinger Band touches
     */
    public MarketContext(
            CandleData candle,
            BollingerBand bollingerBand,
            List<TouchResult> touchResults) {

        this.candle = Objects.requireNonNull(candle, "candle");
        this.bollingerBand = Objects.requireNonNull(
                bollingerBand,
                "bollingerBand");

        this.touchResults = List.copyOf(
                Objects.requireNonNull(
                        touchResults,
                        "touchResults"));
    }

    /**
     * Returns the current candle.
     *
     * @return candle
     */
    public CandleData getCandle() {
        return candle;
    }

    /**
     * Returns the Bollinger Band values.
     *
     * @return Bollinger Band
     */
    public BollingerBand getBollingerBand() {
        return bollingerBand;
    }

    /**
     * Returns the detected Bollinger Band touches.
     *
     * @return immutable touch results
     */
    public List<TouchResult> getTouchResults() {
        return touchResults;
    }

}