package com.edison.scanner.mapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.edison.scanner.common.Timeframe;
import com.edison.scanner.exceptions.ExchangeException;
import com.edison.scanner.model.market.Candle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Maps Binance Kline API responses into {@link Candle} domain objects.
 *
 * <p>
 * This class understands the JSON format returned by Binance and converts it
 * into the application's market model.
 * </p>
 */
public final class BinanceFuturesCandleMapper {

    /**
     * Shared Jackson object mapper.
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Converts a Binance Kline JSON response into a list of {@link Candle}s.
     *
     * @param symbol trading symbol
     * @param timeframe candle timeframe
     * @param json Binance JSON response
     *
     * @return list of candles
     */
    public List<Candle> map(
            String symbol,
            Timeframe timeframe,
            String json) {

    	if (symbol == null || symbol.isBlank()) {
    	    throw new IllegalArgumentException("symbol must not be blank.");
    	}

    	if (timeframe == null) {
    	    throw new IllegalArgumentException("timeframe must not be null.");
    	}

    	if (json == null || json.isBlank()) {
    	    throw new IllegalArgumentException("json must not be blank.");
    	}
    	
        try {

            JsonNode root = OBJECT_MAPPER.readTree(json);

            List<Candle> candles = new ArrayList<>(root.size());

            for (JsonNode node : root) {

                candles.add(mapCandle(
                        symbol,
                        timeframe,
                        node));

            }

            return List.copyOf(candles);

        } catch (JsonProcessingException ex) {
        	ex.printStackTrace();

            throw new ExchangeException(
                    "Unable to parse Binance candle response.",
                    ex);

        }

    }

    /**
     * Converts a single Binance Kline into a {@link Candle}.
     *
     * Binance indexes:
     *
     * <pre>
     * 0 Open Time
     * 1 Open
     * 2 High
     * 3 Low
     * 4 Close
     * 5 Volume
     * 6 Close Time
     * </pre>
     */
    private Candle mapCandle(
            String symbol,
            Timeframe timeframe,
            JsonNode node) {
    	
    	if (node == null || !node.isArray() || node.size() < 7) {
    	    throw new ExchangeException("Invalid Binance candle structure.");
    	}

        return new Candle(
                symbol,
                timeframe,
                Instant.ofEpochMilli(node.get(0).asLong()),
                Instant.ofEpochMilli(node.get(6).asLong()),
                new BigDecimal(node.get(1).asText()),
                new BigDecimal(node.get(2).asText()),
                new BigDecimal(node.get(3).asText()),
                new BigDecimal(node.get(4).asText()),
                new BigDecimal(node.get(5).asText()));

    }

}