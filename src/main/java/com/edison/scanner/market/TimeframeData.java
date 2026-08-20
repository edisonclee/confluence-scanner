package com.edison.scanner.market;

import com.edison.scanner.model.indicator.BollingerBand;
import com.edison.scanner.model.market.HeikenAshiCandle;

/**
 * Contains only the latest market data required by the scanner.
 */
public final class TimeframeData {

    private final HeikenAshiCandle latestHeikenAshi;

    private final BollingerBand latestBollingerBand;

    public TimeframeData(
            HeikenAshiCandle latestHeikenAshi,
            BollingerBand latestBollingerBand) {

        this.latestHeikenAshi = latestHeikenAshi;
        this.latestBollingerBand = latestBollingerBand;

    }

    public HeikenAshiCandle getLatestHeikenAshi() {
        return latestHeikenAshi;
    }

    public BollingerBand getLatestBollingerBand() {
        return latestBollingerBand;
    }

}