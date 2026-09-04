package com.edison.scanner.strategy.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.edison.scanner.common.Timeframe;
import com.edison.scanner.config.ApplicationConfig;
import com.edison.scanner.detector.TouchDetector;
import com.edison.scanner.market.MarketData;
import com.edison.scanner.market.TimeframeData;
import com.edison.scanner.model.ScanResult;
import com.edison.scanner.model.ScannerRequest;
import com.edison.scanner.model.indicator.BollingerBand;
import com.edison.scanner.model.market.HeikenAshiCandle;
import com.edison.scanner.strategy.ScannerStrategy;

@Component
public final class BollingerBandStrategy implements ScannerStrategy {

	private final ApplicationConfig config;

	private final TouchDetector touchDetector;

	public BollingerBandStrategy(ApplicationConfig config, TouchDetector touchDetector) {

		this.config = Objects.requireNonNull(config);
		this.touchDetector = Objects.requireNonNull(touchDetector);

	}

	public List<ScanResult> scan(MarketData marketData, ScannerRequest request) {

		Objects.requireNonNull(marketData, "marketData");

		Objects.requireNonNull(request, "request");

		List<ScanResult> results = new ArrayList<>();

		//
		// H1 is always the chart timeframe.
		//
		Timeframe chartTimeframe = Timeframe.H1;

		TimeframeData chart = marketData.getChart(chartTimeframe);

		if (chart == null) {
			return List.of();
		}

		List<HeikenAshiCandle> ha = chart.getHeikenAshiCandles();

		if (ha.isEmpty()) {
			return List.of();
		}

		HeikenAshiCandle latestChart = ha.get(ha.size() - 1);

		//
		// Scan only the BB timeframes selected
		// by the user.
		//
		for (Timeframe bbTimeframe : request.getBbTimeframes()) {

			TimeframeData bb = marketData.getChart(bbTimeframe);

			if (bb == null) {
				continue;
			}

			BollingerBand latestBand = bb.getLatestBollingerBand();

			if (latestBand == null) {
				continue;
			}

			if (!touchDetector.isTouched(latestChart, latestBand)) {

				continue;
			}

			BigDecimal bbWidthPercent = latestBand.getUpperBand().subtract(latestBand.getLowerBand())
					.divide(latestBand.getBasisBand(), 8, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

			results.add(new ScanResult(marketData.getSymbol().getExchangeSymbol(), bbTimeframe, bbWidthPercent));

		}

		return List.copyOf(results);

	}

	@Override
	public String getName() {
		return "bb";
	}

	public ApplicationConfig getConfig() {
		return config;
	}

}