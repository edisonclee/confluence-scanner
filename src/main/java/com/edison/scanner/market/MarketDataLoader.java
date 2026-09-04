package com.edison.scanner.market;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.edison.scanner.common.Timeframe;
import com.edison.scanner.config.ApplicationConfig;
import com.edison.scanner.converter.HeikenAshiConverter;
import com.edison.scanner.indicator.BollingerBandCalculator;
import com.edison.scanner.indicator.RsiCalculator;
import com.edison.scanner.mapper.BitunixFuturesClient;
import com.edison.scanner.model.indicator.BollingerBand;
import com.edison.scanner.model.indicator.Rsi;
import com.edison.scanner.model.market.Candle;
import com.edison.scanner.model.market.HeikenAshiCandle;
import com.edison.scanner.model.market.TradingSymbol;

/**
 * Loads all market data for one trading symbol.
 */
public final class MarketDataLoader {

	private final ApplicationConfig config;

	private final BitunixFuturesClient client;

	private final HeikenAshiConverter heikenAshiConverter;

	private final BollingerBandCalculator bollingerBandCalculator;

	private final RsiCalculator rsiCalculator;

	private final MarketDataCache cache;

	public MarketDataLoader(ApplicationConfig config, BitunixFuturesClient client,
			HeikenAshiConverter heikenAshiConverter, BollingerBandCalculator bollingerBandCalculator,
			RsiCalculator rsiCalculator, MarketDataCache cache) {

		this.config = Objects.requireNonNull(config);
		this.client = Objects.requireNonNull(client);
		this.heikenAshiConverter = Objects.requireNonNull(heikenAshiConverter);
		this.bollingerBandCalculator = Objects.requireNonNull(bollingerBandCalculator);
		this.cache = Objects.requireNonNull(cache);
		this.rsiCalculator = rsiCalculator;

	}

	/**
	 * Returns cached market data.
	 *
	 * @param symbol trading symbol
	 *
	 * @return cached market data
	 */
	public MarketData get(TradingSymbol symbol) {

		return cache.get(symbol);

	}

	/**
	 * Downloads and caches market data.
	 *
	 * @param symbol trading symbol
	 */
	public void preload(TradingSymbol symbol, Set<Timeframe> requiredTimeframes) {

		if (cache.contains(symbol)) {
			return;
		}

		MarketData marketData = new MarketData(symbol);

		for (Timeframe timeframe : requiredTimeframes) {

			List<Candle> candles = client.getCandles(symbol.getExchangeSymbol(), timeframe,
					config.getScannerCandleLimit());

			List<HeikenAshiCandle> ha = heikenAshiConverter.convert(candles);

			if (ha.size() > config.getScannerHaWarmup()) {
				ha = ha.subList(config.getScannerHaWarmup(), ha.size());
			}

			List<BollingerBand> bands = bollingerBandCalculator.calculate(ha, config.getBbLength(),
					config.getBbMultiplier());

			List<Rsi> rsiValues = rsiCalculator.calculate(ha, 30);

			marketData.put(timeframe,
					new TimeframeData(candles, ha, bands.isEmpty() ? null : bands.get(bands.size() - 1), rsiValues));

		}

		cache.put(marketData);

	}

	public void load(MarketData marketData, Set<Timeframe> requiredTimeframes) {

		TradingSymbol symbol = marketData.getSymbol();

		for (Timeframe timeframe : requiredTimeframes) {

			List<Candle> candles = client.getCandles(symbol.getExchangeSymbol(), timeframe,
					config.getScannerCandleLimit());

			List<HeikenAshiCandle> ha = heikenAshiConverter.convert(candles);

			if (ha.size() > config.getScannerHaWarmup()) {
				ha = ha.subList(config.getScannerHaWarmup(), ha.size());
			}

			List<BollingerBand> bands = bollingerBandCalculator.calculate(ha, config.getBbLength(),
					config.getBbMultiplier());
			List<Rsi> rsiValues = rsiCalculator.calculate(ha, 30);

			marketData.put(timeframe,
					new TimeframeData(candles, ha, bands.isEmpty() ? null : bands.get(bands.size() - 1), rsiValues));

		}

	}
}