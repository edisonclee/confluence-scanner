package com.edison.scanner.model;

import com.edison.scanner.model.indicator.BollingerBand;

import java.util.Objects;

/**
 * Represents all information produced by the scanner for a single candle.
 *
 * <p>This object is the bridge between the scanning layer and the strategy
 * engine.
 */
public final class ScanCandidate {

    /**
     * Market candle.
     */
    private final CandleData candle;

    /**
     * Calculated Bollinger Band.
     */
    private final BollingerBand bollingerBand;

    /**
     * Touch detection result.
     */
    private final TouchResult touchResult;

    /**
     * Creates a scan candidate.
     *
     * @param candle market candle
     * @param bollingerBand Bollinger Band values
     * @param touchResult detected touch
     */
    public ScanCandidate(
            CandleData candle,
            BollingerBand bollingerBand,
            TouchResult touchResult) {

        this.candle = Objects.requireNonNull(candle, "candle");
        this.bollingerBand = Objects.requireNonNull(bollingerBand, "bollingerBand");
        this.touchResult = Objects.requireNonNull(touchResult, "touchResult");

    }

    public CandleData getCandle() {
        return candle;
    }

    public BollingerBand getBollingerBand() {
        return bollingerBand;
    }

    public TouchResult getTouchResult() {
        return touchResult;
    }

}