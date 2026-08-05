package com.edison.scanner.detector;

import com.edison.scanner.model.CandleData;
import com.edison.scanner.model.indicator.BollingerBand;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Detects whether a candle touches any Bollinger Band.
 */
public final class TouchDetector {

    /**
     * Returns whether the candle touches any Bollinger Band.
     *
     * @param candle source candle
     * @param band calculated Bollinger Band
     * @return true if any band is touched
     */
    public boolean isTouched(
            CandleData candle,
            BollingerBand band) {

        Objects.requireNonNull(candle, "candle");
        Objects.requireNonNull(band, "band");

        return isTouched(candle, band.getUpperBand())
                || isTouched(candle, band.getBasisBand())
                || isTouched(candle, band.getLowerBand());

    }

    /**
     * Returns whether the candle intersects the specified band.
     */
    private boolean isTouched(
            CandleData candle,
            BigDecimal bandValue) {

        return candle.getLow().compareTo(bandValue) <= 0
                && candle.getHigh().compareTo(bandValue) >= 0;

    }

}