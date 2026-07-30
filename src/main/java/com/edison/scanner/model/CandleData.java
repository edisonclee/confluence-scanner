package com.edison.scanner.model;

import com.edison.scanner.common.Timeframe;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a generic candle.
 *
 * <p>
 * Both market candles and calculated candles implement this interface,
 * allowing indicator calculators to work with either type.
 * </p>
 */
public interface CandleData {

    String getSymbol();

    Timeframe getTimeframe();

    Instant getOpenTime();

    Instant getCloseTime();

    BigDecimal getOpen();

    BigDecimal getHigh();

    BigDecimal getLow();

    BigDecimal getClose();

    BigDecimal getVolume();

}