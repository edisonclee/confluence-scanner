package com.edison.scanner.market;

import java.util.List;
import java.util.Objects;

import com.edison.scanner.model.indicator.BollingerBand;
import com.edison.scanner.model.market.Candle;
import com.edison.scanner.model.market.HeikenAshiCandle;

/**
 * Contains all market data for one timeframe.
 */
public final class TimeframeData {

    /**
     * Original candles.
     */
    private final List<Candle> candles;

    /**
     * Heiken Ashi candles.
     */
    private final List<HeikenAshiCandle> heikenAshiCandles;

    /**
     * Bollinger Bands.
     */
    private final List<BollingerBand> bollingerBands;

    public TimeframeData(
            List<Candle> candles,
            List<HeikenAshiCandle> heikenAshiCandles,
            List<BollingerBand> bollingerBands) {

        this.candles = List.copyOf(
                Objects.requireNonNull(candles));

        this.heikenAshiCandles = List.copyOf(
                Objects.requireNonNull(heikenAshiCandles));

        this.bollingerBands = List.copyOf(
                Objects.requireNonNull(bollingerBands));

    }

    public List<Candle> getCandles() {
        return candles;
    }

    public List<HeikenAshiCandle> getHeikenAshiCandles() {
        return heikenAshiCandles;
    }

    public List<BollingerBand> getBollingerBands() {
        return bollingerBands;
    }

    /**
     * Returns the latest Heiken Ashi candle.
     */
    public HeikenAshiCandle getLatestHeikenAshi() {

        if (heikenAshiCandles.isEmpty()) {
            return null;
        }

        return heikenAshiCandles.get(
                heikenAshiCandles.size() - 1);

    }

    /**
     * Returns the latest Bollinger Band.
     */
    public BollingerBand getLatestBollingerBand() {

        if (bollingerBands.isEmpty()) {
            return null;
        }

        return bollingerBands.get(
                bollingerBands.size() - 1);

    }

    /**
     * Returns the latest candle.
     */
    public Candle getLatestCandle() {

        if (candles.isEmpty()) {
            return null;
        }

        return candles.get(
                candles.size() - 1);

    }

}