package com.edison.scanner.indicator;

import com.edison.scanner.model.CandleData;
import com.edison.scanner.model.indicator.BollingerBand;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Calculates Bollinger Bands using a Simple Moving Average (SMA)
 * and population standard deviation.
 */
public final class BollingerBandCalculator {

    /**
     * Precision used for all BigDecimal calculations.
     */
    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

    /**
     * Number of decimal places used for square root iterations.
     */
    private static final int SCALE = 16;

    /**
     * Calculates Bollinger Bands.
     *
     * @param candles source candles
     * @param length moving average length
     * @param multiplier standard deviation multiplier
     * @return calculated Bollinger Bands
     */
    public List<BollingerBand> calculate(
            List<? extends CandleData> candles,
            int length,
            BigDecimal multiplier) {

        Objects.requireNonNull(candles, "candles");
        Objects.requireNonNull(multiplier, "multiplier");
        if (multiplier.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "multiplier must be greater than zero.");
        }

        if (length <= 0) {
            throw new IllegalArgumentException("length must be greater than zero.");
        }

        if (candles.size() < length) {
            return List.of();
        }

        List<BollingerBand> bands = new ArrayList<>();

        for (int end = length - 1; end < candles.size(); end++) {

            List<? extends CandleData> window =
                    candles.subList(end - length + 1, end + 1);

            BigDecimal basis = calculateSma(window);

            BigDecimal deviation = calculateStandardDeviation(window, basis);

            BigDecimal offset = deviation.multiply(multiplier, MATH_CONTEXT);

            CandleData candle = candles.get(end);

            bands.add(new BollingerBand(
                    candle.getSymbol(),
                    candle.getTimeframe(),
                    candle.getOpenTime(),
                    candle.getCloseTime(),
                    basis.add(offset, MATH_CONTEXT),
                    basis,
                    basis.subtract(offset, MATH_CONTEXT)
            ));
        }

        return List.copyOf(bands);
    }

    /**
     * Calculates the Simple Moving Average.
     */
    private BigDecimal calculateSma(List<? extends CandleData> candles) {

        BigDecimal sum = BigDecimal.ZERO;

        for (CandleData candle : candles) {
            sum = sum.add(candle.getClose(), MATH_CONTEXT);
        }

        return sum.divide(
                BigDecimal.valueOf(candles.size()),
                MATH_CONTEXT);
    }

    /**
     * Calculates the population standard deviation.
     */
    private BigDecimal calculateStandardDeviation(
            List<? extends CandleData> candles,
            BigDecimal mean) {

        BigDecimal variance = BigDecimal.ZERO;

        for (CandleData candle : candles) {

            BigDecimal difference =
                    candle.getClose().subtract(mean, MATH_CONTEXT);

            variance = variance.add(
                    difference.multiply(difference, MATH_CONTEXT),
                    MATH_CONTEXT);
        }

        variance = variance.divide(
                BigDecimal.valueOf(candles.size()),
                MATH_CONTEXT);

        return sqrt(variance);
    }

    /**
     * Computes the square root using the Newton-Raphson method.
     */
    private BigDecimal sqrt(BigDecimal value) {

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ArithmeticException("Square root of negative value.");
        }

        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal x = value;
        BigDecimal previous;

        do {

            previous = x;

            x = value.divide(x, SCALE, RoundingMode.HALF_UP)
                    .add(x)
                    .divide(BigDecimal.valueOf(2), SCALE, RoundingMode.HALF_UP);

        } while (!x.equals(previous));

        return x;
    }

}