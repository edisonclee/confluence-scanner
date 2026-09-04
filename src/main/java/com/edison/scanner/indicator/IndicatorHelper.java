package com.edison.scanner.indicator;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import com.edison.scanner.model.indicator.Rsi;
import com.edison.scanner.model.market.HeikenAshiCandle;

/**
 * Shared helper methods used by indicator-based strategies.
 */
@Component
public class IndicatorHelper {

	public boolean hasRecentOversoldTouch(List<Rsi> rsiValues, int lookback, BigDecimal oversold) {

		if (rsiValues.size() < 2) {
			return false;
		}

		int end = rsiValues.size() - 2;
		int start = Math.max(0, end - lookback + 1);

		for (int i = end; i >= start; i--) {

			if (rsiValues.get(i).getValue().compareTo(oversold) <= 0) {

				return true;

			}

		}

		return false;

	}

	public boolean hasRecentOverboughtTouch(List<Rsi> rsiValues, int lookback, BigDecimal overbought) {

		if (rsiValues.size() < 2) {
			return false;
		}

		int end = rsiValues.size() - 2;
		int start = Math.max(0, end - lookback + 1);

		for (int i = end; i >= start; i--) {

			if (rsiValues.get(i).getValue().compareTo(overbought) >= 0) {

				return true;

			}

		}

		return false;

	}

	public boolean isBullishReversal(List<HeikenAshiCandle> candles) {

		if (candles.size() < 3) {
			return false;
		}

		HeikenAshiCandle latestClosed = candles.get(candles.size() - 2);

		HeikenAshiCandle previousClosed = candles.get(candles.size() - 3);

		return isBearish(previousClosed) && isBullish(latestClosed);

	}

	public boolean isBearishReversal(List<HeikenAshiCandle> candles) {

		if (candles.size() < 3) {
			return false;
		}

		HeikenAshiCandle latestClosed = candles.get(candles.size() - 2);

		HeikenAshiCandle previousClosed = candles.get(candles.size() - 3);

		return isBullish(previousClosed) && isBearish(latestClosed);

	}

	private boolean isBullish(HeikenAshiCandle candle) {

		return candle.getClose().compareTo(candle.getOpen()) > 0;

	}

	private boolean isBearish(HeikenAshiCandle candle) {

		return candle.getClose().compareTo(candle.getOpen()) < 0;

	}

	public static boolean wasRecentlyOversold(List<Rsi> values) {

		return wasRecentlyOversold(values, 24);

	}

	public static boolean wasRecentlyOversold(List<Rsi> values, int lookback) {

		int start = Math.max(0, values.size() - lookback);

		for (int i = start; i < values.size(); i++) {

			if (values.get(i).getValue().doubleValue() <= 30.0) {
				return true;
			}

		}

		return false;

	}

	public static boolean wasRecentlyOverbought(List<Rsi> values) {

		return wasRecentlyOverbought(values, 24);

	}

	public static boolean wasRecentlyOverbought(List<Rsi> values, int lookback) {

		int start = Math.max(0, values.size() - lookback);

		for (int i = start; i < values.size(); i++) {

			if (values.get(i).getValue().doubleValue() >= 70.0) {
				return true;
			}

		}

		return false;

	}

}