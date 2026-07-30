package com.edison.scanner.converter;

import com.edison.scanner.model.CandleData;
import com.edison.scanner.model.market.HeikenAshiCandle;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts market candles into Heiken Ashi candles.
 */
public final class HeikenAshiConverter {

    /**
     * Shared math context used for all calculations.
     */
    private static final MathContext MATH_CONTEXT =
            MathContext.DECIMAL64;

    private static final BigDecimal TWO =
            BigDecimal.valueOf(2);

    private static final BigDecimal FOUR =
            BigDecimal.valueOf(4);

    /**
     * Converts market candles into Heiken Ashi candles.
     *
     * @param candles market candles
     *
     * @return immutable Heiken Ashi candles
     */
    public List<HeikenAshiCandle> convert(
            List<? extends CandleData> candles) {

        if (candles.isEmpty()) {
            return List.of();
        }

        List<HeikenAshiCandle> result =
                new ArrayList<>(candles.size());

        HeikenAshiCandle previous = null;

        for (CandleData candle : candles) {

            HeikenAshiCandle current =
                    convert(candle, previous);

            result.add(current);

            previous = current;

        }

        return List.copyOf(result);

    }

    /**
     * Converts one candle.
     */
    private HeikenAshiCandle convert(
            CandleData candle,
            HeikenAshiCandle previous) {

        BigDecimal haClose =
                calculateClose(candle);

        BigDecimal haOpen =
                previous == null
                        ? calculateFirstOpen(candle)
                        : calculateOpen(previous);

        BigDecimal haHigh =
                max(
                        candle.getHigh(),
                        haOpen,
                        haClose);

        BigDecimal haLow =
                min(
                        candle.getLow(),
                        haOpen,
                        haClose);

        return new HeikenAshiCandle(
                candle.getSymbol(),
                candle.getTimeframe(),
                candle.getOpenTime(),
                candle.getCloseTime(),
                haOpen,
                haHigh,
                haLow,
                haClose,
                candle.getVolume());

    }

    /**
     * HA Close = (O+H+L+C)/4
     */
    private BigDecimal calculateClose(
    		CandleData candle) {

        return candle.getOpen()
                .add(candle.getHigh())
                .add(candle.getLow())
                .add(candle.getClose())
                .divide(FOUR, MATH_CONTEXT);

    }

    /**
     * First HA Open = (O+C)/2
     */
    private BigDecimal calculateFirstOpen(
    		CandleData candle) {

        return candle.getOpen()
                .add(candle.getClose())
                .divide(TWO, MATH_CONTEXT);

    }

    /**
     * HA Open = (Previous HA Open + Previous HA Close)/2
     */
    private BigDecimal calculateOpen(
            HeikenAshiCandle previous) {

        return previous.getOpen()
                .add(previous.getClose())
                .divide(TWO, MATH_CONTEXT);

    }

    private BigDecimal max(
            BigDecimal a,
            BigDecimal b,
            BigDecimal c) {

        return a.max(b).max(c);

    }

    private BigDecimal min(
            BigDecimal a,
            BigDecimal b,
            BigDecimal c) {

        return a.min(b).min(c);

    }

}