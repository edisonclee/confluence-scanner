package com.edison.scanner.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.edison.scanner.common.Timeframe;
import com.edison.scanner.model.market.Candle;
import com.edison.scanner.model.market.HeikenAshiCandle;

class HeikenAshiConverterTest {

	@Test
	void shouldReturnEmptyListWhenInputIsEmpty() {

	    // Arrange
	    HeikenAshiConverter converter = new HeikenAshiConverter();

	    // Act
	    var result = converter.convert(Collections.emptyList());

	    // Assert
	    assertTrue(result.isEmpty());

	}
	
	@Test
	void shouldThrowExceptionWhenInputIsNull() {

	    // Arrange
	    HeikenAshiConverter converter = new HeikenAshiConverter();

	    // Act & Assert
	    assertThrows(
	            NullPointerException.class,
	            () -> converter.convert(null));

	}
	
	@Test
	void testCandle() {
		Candle candle = new Candle(
		        "BTCUSDT",
		        Timeframe.H1,
		        Instant.parse("2026-01-01T00:00:00Z"),
		        Instant.parse("2026-01-01T01:00:00Z"),
		        new BigDecimal("100"),
		        new BigDecimal("110"),
		        new BigDecimal("90"),
		        new BigDecimal("105"),
		        new BigDecimal("1000"));
		
		HeikenAshiConverter converter = new HeikenAshiConverter();

		List<HeikenAshiCandle> result =
		        converter.convert(List.of(candle));
		
		assertEquals(1, result.size());
		
		HeikenAshiCandle ha = result.get(0);
		
		assertEquals(new BigDecimal("102.5"), ha.getOpen());

		assertEquals(new BigDecimal("101.25"), ha.getClose());

		assertEquals(new BigDecimal("110"), ha.getHigh());

		assertEquals(new BigDecimal("90"), ha.getLow());

		assertEquals("BTCUSDT", ha.getSymbol());

		assertEquals(Timeframe.H1, ha.getTimeframe());

		assertEquals(new BigDecimal("1000"), ha.getVolume());

		assertEquals(candle.getOpenTime(), ha.getOpenTime());

		assertEquals(candle.getCloseTime(), ha.getCloseTime());
		
	}

}