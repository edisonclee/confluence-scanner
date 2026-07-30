package com.edison.scanner.scanner;

import com.edison.scanner.detector.TouchDetector;
import com.edison.scanner.indicator.BollingerBandCalculator;
import com.edison.scanner.model.CandleData;
import com.edison.scanner.model.TouchResult;
import com.edison.scanner.model.indicator.BollingerBand;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Scans candle data for Bollinger Band touches.
 *
 * <p>
 * This class orchestrates the Bollinger Band calculation and touch detection.
 * It is independent of the candle implementation and can scan raw candles,
 * Heiken Ashi candles, or any future CandleData implementation.
 * </p>
 */
public final class ConfluenceScanner {

    private final BollingerBandCalculator bollingerBandCalculator;

    private final TouchDetector touchDetector;

    /**
     * Creates a ConfluenceScanner.
     *
     * @param bollingerBandCalculator Bollinger Band calculator
     * @param touchDetector touch detector
     */
    public ConfluenceScanner(
            BollingerBandCalculator bollingerBandCalculator,
            TouchDetector touchDetector) {

        this.bollingerBandCalculator =
                Objects.requireNonNull(
                        bollingerBandCalculator,
                        "bollingerBandCalculator");

        this.touchDetector =
                Objects.requireNonNull(
                        touchDetector,
                        "touchDetector");

    }

    /**
     * Executes the Bollinger Band touch scan.
     *
     * @param candles source candle data
     * @param length Bollinger Band length
     * @param multiplier standard deviation multiplier
     * @return detected touches
     */
    public List<TouchResult> scan(
            List<? extends CandleData> candles,
            int length,
            BigDecimal multiplier) {

        Objects.requireNonNull(candles, "candles");
        Objects.requireNonNull(multiplier, "multiplier");

        if (candles.isEmpty()) {
            return List.of();
        }

        List<BollingerBand> bands =
                bollingerBandCalculator.calculate(
                        candles,
                        length,
                        multiplier);

        List<TouchResult> results = new ArrayList<>();

        for (int bandIndex = 0; bandIndex < bands.size(); bandIndex++) {

            int candleIndex = bandIndex + length - 1;

            CandleData candle = candles.get(candleIndex);

            results.addAll(
                    touchDetector.detect(
                            candle,
                            bands.get(bandIndex)));

        }

        return List.copyOf(results);

    }

}