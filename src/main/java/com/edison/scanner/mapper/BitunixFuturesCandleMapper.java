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
 * Maps Bitunix Kline API responses into Candle objects.
 */
public final class BitunixFuturesCandleMapper {

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper();

    public List<Candle> map(
            String symbol,
            Timeframe timeframe,
            String json) {

        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException(
                    "symbol must not be blank.");
        }

        if (timeframe == null) {
            throw new IllegalArgumentException(
                    "timeframe must not be null.");
        }

        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException(
                    "json must not be blank.");
        }

        try {

            JsonNode root =
                    OBJECT_MAPPER.readTree(json);

            if (root.get("code").asInt() != 0) {

                throw new ExchangeException(
                        "Bitunix error: "
                                + root.get("msg").asText());

            }

            JsonNode data =
                    root.get("data");

            List<Candle> candles =
                    new ArrayList<>(data.size());

            for (JsonNode node : data) {

                candles.add(

                        mapCandle(

                                symbol,

                                timeframe,

                                node));

            }

            return candles;

        } catch (JsonProcessingException ex) {

            throw new ExchangeException(

                    "Unable to parse Bitunix candle response.",

                    ex);

        }

    }

    private Candle mapCandle(

            String symbol,

            Timeframe timeframe,

            JsonNode node) {

        Instant openTime =
                Instant.ofEpochMilli(

                        node.get("time").asLong());

        Instant closeTime =
                openTime.plus(

                        timeframe.getDuration());

        return new Candle(

                symbol,

                timeframe,

                openTime,

                closeTime,

                new BigDecimal(node.get("open").asText()),

                new BigDecimal(node.get("high").asText()),

                new BigDecimal(node.get("low").asText()),

                new BigDecimal(node.get("close").asText()),

                new BigDecimal(node.get("baseVol").asText()));

    }

}