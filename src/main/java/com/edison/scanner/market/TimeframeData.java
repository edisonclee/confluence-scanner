package com.edison.scanner.market;

import java.util.List;

import com.edison.scanner.model.indicator.BollingerBand;
import com.edison.scanner.model.indicator.Rsi;
import com.edison.scanner.model.market.Candle;
import com.edison.scanner.model.market.HeikenAshiCandle;

/**
 * Contains only the latest market data required by the scanner.
 */
public final class TimeframeData {

	private final List<Candle> candles;

	private final List<HeikenAshiCandle> heikenAshiCandles;

	private final BollingerBand latestBollingerBand;

	private final List<Rsi> rsiValues;

	public TimeframeData(
	        List<Candle> candles,
	        List<HeikenAshiCandle> heikenAshiCandles,
	        BollingerBand latestBollingerBand,
	        List<Rsi> rsiValues) {

	    this.candles = List.copyOf(candles);

	    this.heikenAshiCandles = List.copyOf(heikenAshiCandles);

	    this.latestBollingerBand = latestBollingerBand;

	    this.rsiValues = List.copyOf(rsiValues);
	}

	public List<HeikenAshiCandle> getHeikenAshiCandles() {
	    return heikenAshiCandles;
	}

    public BollingerBand getLatestBollingerBand() {
        return latestBollingerBand;
    }

	public List<Rsi> getRsiValues() {
		return rsiValues;
	}

	public List<Candle> getCandles() {
		return candles;
	}

}