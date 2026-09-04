package com.edison.scanner.converter;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;

import org.springframework.stereotype.Component;

import com.edison.scanner.model.market.Candle;
import com.edison.scanner.model.market.HeikenAshiCandle;

@Component
public class LiveHeikenAshiBuilder {

	private static final MathContext MC = MathContext.DECIMAL64;

	private static final BigDecimal TWO = BigDecimal.valueOf(2);

	private static final BigDecimal FOUR = BigDecimal.valueOf(4);

	public HeikenAshiCandle build(HeikenAshiCandle previousHa, Candle previousReal, BigDecimal currentPrice) {

		/*
		 * Current REAL candle.
		 */
		BigDecimal open = previousReal.getClose();

		BigDecimal high = open.max(currentPrice);

		BigDecimal low = open.min(currentPrice);

		BigDecimal close = currentPrice;

		/*
		 * Live HA candle.
		 */
		BigDecimal haOpen = previousHa.getOpen().add(previousHa.getClose()).divide(TWO, MC);

		BigDecimal haClose = open.add(high).add(low).add(close).divide(FOUR, MC);

		BigDecimal haHigh = high.max(haOpen).max(haClose);

		BigDecimal haLow = low.min(haOpen).min(haClose);

		Instant openTime = previousReal.getCloseTime();

		Instant closeTime = openTime.plus(previousReal.getTimeframe().getDuration());

		return new HeikenAshiCandle(previousReal.getSymbol(), previousReal.getTimeframe(), openTime, closeTime, haOpen,
				haHigh, haLow, haClose, BigDecimal.ZERO);
	}

}