
package com.edison.scanner.strategy.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger log = LoggerFactory.getLogger(BollingerBandStrategy.class);

	// Temporary focused diagnostic. Remove this symbol after verification.
	private static final String DIAGNOSTIC_SYMBOL = "SOLUSDT";

	private final ApplicationConfig config;
	private final TouchDetector touchDetector;

	public BollingerBandStrategy(ApplicationConfig config, TouchDetector touchDetector) {

		this.config = Objects.requireNonNull(config);
		this.touchDetector = Objects.requireNonNull(touchDetector);
	}

	@Override
	public List<ScanResult> scan(MarketData marketData, ScannerRequest request) {

		Objects.requireNonNull(marketData, "marketData");
		Objects.requireNonNull(request, "request");

		List<ScanResult> results = new ArrayList<>();

		String symbol = marketData.getSymbol().getExchangeSymbol();

		boolean diagnostic = DIAGNOSTIC_SYMBOL.equals(symbol);

		// H1 is the chart timeframe; BB timeframe comes from the request.
		TimeframeData chart = marketData.getChart(Timeframe.H1);

		if (chart == null) {
			if (diagnostic) {
				log.info("[BB-PRICE-CHECK] symbol={} chartTF=H1 status=NO_CHART", symbol);
			}
			return List.of();
		}

		List<HeikenAshiCandle> candles = chart.getHeikenAshiCandles();

		if (candles.isEmpty()) {
			if (diagnostic) {
				log.info("[BB-PRICE-CHECK] symbol={} chartTF=H1 status=NO_CANDLES", symbol);
			}
			return List.of();
		}

		HeikenAshiCandle latestChart = candles.get(candles.size() - 1);

		// This is the latest H1 Heiken Ashi close, not necessarily
		// the exchange's live market price.
		BigDecimal price = latestChart.getClose();

		for (Timeframe bbTimeframe : request.getBbTimeframes()) {

			TimeframeData bbData = marketData.getChart(bbTimeframe);

			if (bbData == null) {
				if (diagnostic) {
					log.info("[BB-PRICE-CHECK] symbol={} chartTF=H1 bbTF={} " + "status=NO_BB_TIMEFRAME_DATA", symbol,
							bbTimeframe);
				}
				continue;
			}

			BollingerBand band = bbData.getLatestBollingerBand();

			if (band == null || band.getUpperBand() == null || band.getBasisBand() == null
					|| band.getLowerBand() == null) {

				if (diagnostic) {
					log.info("[BB-PRICE-CHECK] symbol={} chartTF=H1 bbTF={} " + "status=NO_VALID_BANDS", symbol,
							bbTimeframe);
				}
				continue;
			}

			BigDecimal upper = band.getUpperBand();
			BigDecimal basis = band.getBasisBand();
			BigDecimal lower = band.getLowerBand();

			boolean upperTouched = touches(latestChart, upper);

			boolean basisTouched = touches(latestChart, basis);

			boolean lowerTouched = touches(latestChart, lower);

			boolean candleTouchesBB = touchDetector.isTouched(latestChart, band);

			// Inclusive range: lower <= price <= upper.
			boolean priceInsideBands = price.compareTo(lower) >= 0 && price.compareTo(upper) <= 0;

			// A symbol qualifies if the H1 candle touches a BB line
			// OR its H1 Heiken Ashi close is inside the selected BB range.
			boolean qualifies = candleTouchesBB || priceInsideBands;

			if (diagnostic) {
				log.info(
						"[BB-PRICE-CHECK] symbol={} chartTF=H1 bbTF={} " + "priceSource=H1_HEIKEN_ASHI_CLOSE price={} "
								+ "upper={} basis={} lower={} " + "priceInsideBands={} candleTouchesBB={} "
								+ "touchUpper={} touchBasis={} touchLower={} " + "qualifies={} reason={}",
						symbol, bbTimeframe, price, upper, basis, lower, priceInsideBands, candleTouchesBB,
						upperTouched, basisTouched, lowerTouched, qualifies,
						qualifies ? (priceInsideBands ? "PRICE_INSIDE_BANDS" : "CANDLE_TOUCH")
								: "OUTSIDE_BANDS_NO_TOUCH");
			}

			if (!qualifies) {
				continue;
			}

			// Prevent division by zero if the BB basis is invalid.
			if (basis.compareTo(BigDecimal.ZERO) == 0) {
				if (diagnostic) {
					log.warn("[BB-PRICE-CHECK] symbol={} bbTF={} " + "status=INVALID_ZERO_BASIS", symbol, bbTimeframe);
				}
				continue;
			}

			BigDecimal bbWidthPercent = upper.subtract(lower).divide(basis, 8, RoundingMode.HALF_UP)
					.multiply(BigDecimal.valueOf(100));

			results.add(new ScanResult(symbol, bbTimeframe, bbWidthPercent));
		}

		return List.copyOf(results);
	}

	/**
	 * Returns true when the candle's high-low range includes the specified
	 * Bollinger Band value.
	 */
	private boolean touches(HeikenAshiCandle candle, BigDecimal bandValue) {

		return candle.getLow().compareTo(bandValue) <= 0 && candle.getHigh().compareTo(bandValue) >= 0;
	}

	@Override
	public String getName() {
		return "bb";
	}

	public ApplicationConfig getConfig() {
		return config;
	}
}
