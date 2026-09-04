package com.edison.scanner.indicator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.edison.scanner.model.indicator.Rsi;
import com.edison.scanner.model.market.HeikenAshiCandle;

/**
 * Calculates RSI values from Heiken Ashi candles.
 */
@Component
public class RsiCalculator {

    public List<Rsi> calculate(
            List<HeikenAshiCandle> candles,
            int length) {

        if (candles.size() <= length) {
            return List.of();
        }

        List<Rsi> values =
                new ArrayList<>();

        BigDecimal gain =
                BigDecimal.ZERO;

        BigDecimal loss =
                BigDecimal.ZERO;

        for (int i = 1; i <= length; i++) {

            BigDecimal change =
                    candles.get(i)
                            .getClose()
                            .subtract(
                                    candles.get(i - 1)
                                            .getClose());

            if (change.signum() >= 0) {

                gain = gain.add(change);

            } else {

                loss = loss.add(change.abs());

            }

        }

        BigDecimal avgGain =
                gain.divide(
                        BigDecimal.valueOf(length),
                        8,
                        RoundingMode.HALF_UP);

        BigDecimal avgLoss =
                loss.divide(
                        BigDecimal.valueOf(length),
                        8,
                        RoundingMode.HALF_UP);

        values.add(
                calculateRsi(
                        avgGain,
                        avgLoss));

        for (int i = length + 1;
                i < candles.size();
                i++) {

            BigDecimal change =
                    candles.get(i)
                            .getClose()
                            .subtract(
                                    candles.get(i - 1)
                                            .getClose());

            BigDecimal currentGain =
                    change.signum() > 0
                            ? change
                            : BigDecimal.ZERO;

            BigDecimal currentLoss =
                    change.signum() < 0
                            ? change.abs()
                            : BigDecimal.ZERO;

            avgGain =
                    avgGain.multiply(
                            BigDecimal.valueOf(length - 1))
                            .add(currentGain)
                            .divide(
                                    BigDecimal.valueOf(length),
                                    8,
                                    RoundingMode.HALF_UP);

            avgLoss =
                    avgLoss.multiply(
                            BigDecimal.valueOf(length - 1))
                            .add(currentLoss)
                            .divide(
                                    BigDecimal.valueOf(length),
                                    8,
                                    RoundingMode.HALF_UP);

            values.add(
                    calculateRsi(
                            avgGain,
                            avgLoss));

        }

        return List.copyOf(values);

    }

    private Rsi calculateRsi(
            BigDecimal avgGain,
            BigDecimal avgLoss) {

        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {

            return new Rsi(
                    BigDecimal.valueOf(100));

        }

        BigDecimal rs =
                avgGain.divide(
                        avgLoss,
                        8,
                        RoundingMode.HALF_UP);

        BigDecimal rsi =
                BigDecimal.valueOf(100)
                        .subtract(
                                BigDecimal.valueOf(100)
                                        .divide(
                                                BigDecimal.ONE.add(rs),
                                                8,
                                                RoundingMode.HALF_UP));

        return new Rsi(rsi);

    }

}