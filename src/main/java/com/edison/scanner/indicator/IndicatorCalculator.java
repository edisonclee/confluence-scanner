package com.edison.scanner.indicator;

import com.edison.scanner.model.CandleData;

import java.util.List;

/**
 * Defines a calculator that produces indicator values from market candles.
 *
 * <p>
 * Implementations are responsible for calculating a specific technical
 * indicator, such as Bollinger Bands, RSI or MACD.
 *
 * @param <T>
 *         indicator model type
 */
public interface IndicatorCalculator<T> {

    /**
     * Calculates indicator values from the supplied candles.
     *
     * @param candles
     *         source candles
     *
     * @return calculated indicator values
     */
    List<T> calculate(
            List<? extends CandleData> candles);

}