package com.edison.scanner.bitunix.mapper;

import java.util.ArrayList;
import java.util.List;

import com.edison.scanner.exceptions.ExchangeException;
import com.edison.scanner.model.market.TradingSymbol;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Maps Bitunix trading pairs into TradingSymbol objects.
 */
public final class BitunixSymbolMapper {

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper();

    public List<TradingSymbol> map(String json) {

        try {

            JsonNode root =
                    OBJECT_MAPPER.readTree(json);

            JsonNode data =
                    root.get("data");

            List<TradingSymbol> symbols =
                    new ArrayList<>();

            for (JsonNode node : data) {

                if (!isSupported(node)) {
                    continue;
                }

                String symbol =
                        node.get("symbol").asText();

                String base =
                        node.get("base").asText();

                String quote =
                        node.get("quote").asText();

                symbols.add(
                        new TradingSymbol(
                                symbol,
                                base,
                                quote));
            }

            return List.copyOf(symbols);

        } catch (Exception ex) {
        	ex.printStackTrace();

			throw new ExchangeException("Failed to communicate with Binance Futures.",ex);
		}

    }

    /**
     * Keep only supported crypto pairs.
     */
    private boolean isSupported(JsonNode node) {

        if (!node.get("symbolStatus")
                .asText()
                .equals("OPEN")) {
            return false;
        }

        if (!node.get("isApiSupported")
                .asBoolean()) {
            return false;
        }

        if (!node.get("quote")
                .asText()
                .equals("USDT")) {
            return false;
        }

        /*
         * Ignore stocks, commodities, ETFs, etc.
         * Crypto symbols have integer base precision.
         */
        return node.get("basePrecision")
                .asInt() == 0;

    }

}