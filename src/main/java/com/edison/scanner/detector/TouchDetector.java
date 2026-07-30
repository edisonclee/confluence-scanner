package com.edison.scanner.detector;

import com.edison.scanner.common.BandType;
import com.edison.scanner.model.CandleData;
import com.edison.scanner.model.TouchResult;
import com.edison.scanner.model.indicator.BollingerBand;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Detects Bollinger Band touches.
 */
public final class TouchDetector {

    /**
     * Detects all Bollinger Band touches for a candle.
     *
     * @param candle source candle
     * @param band calculated Bollinger Band
     * @return detected touches
     */
    public List<TouchResult> detect(
            CandleData candle,
            BollingerBand band) {

        Objects.requireNonNull(candle, "candle");
        Objects.requireNonNull(band, "band");

        List<TouchResult> touches = new ArrayList<>(3);

        detectTouch(candle, band.getUpperBand(), BandType.UPPER, touches);
        detectTouch(candle, band.getBasisBand(), BandType.BASIS, touches);
        detectTouch(candle, band.getLowerBand(), BandType.LOWER, touches);

        return List.copyOf(touches);
    }

    private void detectTouch(
            CandleData candle,
            BigDecimal bandValue,
            BandType bandType,
            List<TouchResult> touches) {

        if (isTouched(candle, bandValue)) {

            touches.add(new TouchResult(
                    candle.getSymbol(),
                    candle.getTimeframe(),
                    candle.getOpenTime(),
                    candle.getCloseTime(),
                    bandType));

        }
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