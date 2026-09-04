package com.edison.scanner.strategy.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.edison.scanner.common.Timeframe;
import com.edison.scanner.converter.LiveHeikenAshiBuilder;
import com.edison.scanner.indicator.IndicatorHelper;
import com.edison.scanner.mapper.BitunixFuturesClient;
import com.edison.scanner.market.MarketData;
import com.edison.scanner.market.TimeframeData;
import com.edison.scanner.model.indicator.Rsi;
import com.edison.scanner.model.market.Candle;
import com.edison.scanner.model.market.HeikenAshiCandle;
import com.edison.scanner.strategy.transition.TransitionDirection;
import com.edison.scanner.strategy.transition.TransitionPlayResult;

@Component
public class TransitionPlayStrategy {

	private final LiveHeikenAshiBuilder liveHeikenAshiBuilder;
	private final BitunixFuturesClient client;

	public TransitionPlayStrategy(BitunixFuturesClient client, LiveHeikenAshiBuilder liveHeikenAshiBuilder) {
		this.client = client;
		this.liveHeikenAshiBuilder = liveHeikenAshiBuilder;
	}

	public Optional<TransitionPlayResult> scan(MarketData marketData) {

		TimeframeData h1 = marketData.getChart(Timeframe.H1);
		TimeframeData h4 = marketData.getChart(Timeframe.H4);

		if (h1 == null || h4 == null) {
			return Optional.empty();
		}

		List<Rsi> rsiValues = h1.getRsiValues();

		boolean oversold = IndicatorHelper.wasRecentlyOversold(rsiValues);
		boolean overbought = IndicatorHelper.wasRecentlyOverbought(rsiValues);

		if (!oversold && !overbought) {
			return Optional.empty();
		}

		List<HeikenAshiCandle> ha = h4.getHeikenAshiCandles();
		List<Candle> candles = h4.getCandles();

		if (ha.size() < 2 || candles.isEmpty()) {
			return Optional.empty();
		}

		BigDecimal currentPrice = client.getCurrentPrice(marketData.getSymbol().getExchangeSymbol());

		HeikenAshiCandle live = liveHeikenAshiBuilder.build(ha.get(ha.size() - 1), candles.get(candles.size() - 1),
				currentPrice);

		HeikenAshiCandle second = ha.get(ha.size() - 2);

		HeikenAshiCandle first = ha.get(ha.size() - 1);

		boolean c1 = isBullish(second);
		boolean c2 = isBullish(first);
		boolean c3 = isBullish(live);

		if (oversold) {

			// RRG
			if (!c1 && !c2 && c3) {
				return Optional.of(new TransitionPlayResult(marketData.getSymbol().getExchangeSymbol(),
						TransitionDirection.LONG, "RRG"));
			}

			// RGG
			if (!c1 && c2 && c3) {
				return Optional.of(new TransitionPlayResult(marketData.getSymbol().getExchangeSymbol(),
						TransitionDirection.LONG, "RGG"));
			}

			// RGR
			if (!c1 && c2 && !c3) {
				return Optional.of(new TransitionPlayResult(marketData.getSymbol().getExchangeSymbol(),
						TransitionDirection.LONG, "RGR"));
			}
		}

		if (overbought) {

			// GGR
			if (c1 && c2 && !c3) {
				return Optional.of(new TransitionPlayResult(marketData.getSymbol().getExchangeSymbol(),
						TransitionDirection.SHORT, "GGR"));
			}

			// GRR
			if (c1 && !c2 && !c3) {
				return Optional.of(new TransitionPlayResult(marketData.getSymbol().getExchangeSymbol(),
						TransitionDirection.SHORT, "GRR"));
			}

			// GRG
			if (c1 && !c2 && c3) {
				return Optional.of(new TransitionPlayResult(marketData.getSymbol().getExchangeSymbol(),
						TransitionDirection.SHORT, "GRG"));
			}
		}

		return Optional.empty();
	}

	public boolean passesRsi(MarketData marketData) {

		TimeframeData h1 = marketData.getChart(Timeframe.H1);

		if (h1 == null) {
			return false;
		}

		List<Rsi> rsiValues = h1.getRsiValues();

		if (rsiValues == null || rsiValues.isEmpty()) {

			return false;

		}

		return IndicatorHelper.wasRecentlyOversold(rsiValues) || IndicatorHelper.wasRecentlyOverbought(rsiValues);

	}

	private boolean isBullish(HeikenAshiCandle candle) {

		return candle.getClose().compareTo(candle.getOpen()) >= 0;

	}

	public LiveHeikenAshiBuilder getLiveHeikenAshiBuilder() {
		return liveHeikenAshiBuilder;
	}

	public BitunixFuturesClient getClient() {
		return client;
	}

}