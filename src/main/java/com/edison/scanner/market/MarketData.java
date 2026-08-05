package com.edison.scanner.market;

import com.edison.scanner.common.Timeframe;
import com.edison.scanner.model.market.TradingSymbol;

import java.util.EnumMap;
import java.util.Map;

/**
 * Contains all downloaded market data for one symbol.
 */
public final class MarketData {

    /**
     * Trading symbol.
     */
    private final TradingSymbol symbol;

    /**
     * Data by timeframe.
     */
    private final Map<Timeframe, TimeframeData> data =
            new EnumMap<>(Timeframe.class);

    public MarketData(
            TradingSymbol symbol) {

        this.symbol = symbol;

    }

    public TradingSymbol getSymbol() {
        return symbol;
    }

    public void put(
            Timeframe timeframe,
            TimeframeData timeframeData) {

        data.put(
                timeframe,
                timeframeData);

    }

    public TimeframeData get(
            Timeframe timeframe) {

        return data.get(timeframe);

    }
    
    /**
     * Returns chart data.
     */
    public TimeframeData getChart(
            Timeframe timeframe) {

        return data.get(timeframe);

    }

    /**
     * Returns higher timeframe data.
     */
    public TimeframeData getHigher(
            Timeframe timeframe) {

        Timeframe higher =
                timeframe.getHigherTimeframe();

        if (higher == null) {
            return null;
        }

        return data.get(higher);

    }

}