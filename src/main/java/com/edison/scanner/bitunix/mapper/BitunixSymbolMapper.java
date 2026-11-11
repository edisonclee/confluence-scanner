package com.edison.scanner.bitunix.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.edison.scanner.bitunix.BitunixContractFilter;
import com.edison.scanner.exceptions.ExchangeException;
import com.edison.scanner.model.market.TradingSymbol;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public final class BitunixSymbolMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BitunixSymbolMapper.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /*
     * IMPORTANT:
     * Scanner universe filtering must not use CryptoAssetRegistry.
     * CryptoAssetRegistry remains available for OI Radar's CoinGecko
     * market-cap/enrichment flow only.
     */
    private final BitunixContractFilter bitunixContractFilter;

    public BitunixSymbolMapper(BitunixContractFilter bitunixContractFilter) {
        this.bitunixContractFilter = Objects.requireNonNull(bitunixContractFilter);
    }

    public List<TradingSymbol> map(String json) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(json);
            JsonNode data = root.path("data");

            if (!data.isArray()) {
                throw new ExchangeException(
                        "Invalid Bitunix trading-pairs response: data is not an array.");
            }

            List<TradingSymbol> symbols = new ArrayList<>();

            int rejectedStatus = 0;
            int rejectedApi = 0;
            int rejectedQuote = 0;
            int rejectedAsset = 0;

            for (JsonNode node : data) {
                if (!"OPEN".equalsIgnoreCase(node.path("symbolStatus").asText())) {
                    rejectedStatus++;
                    continue;
                }

                if (!node.path("isApiSupported").asBoolean(false)) {
                    rejectedApi++;
                    continue;
                }

                String symbol = node.path("symbol").asText("").trim();
                String base = node.path("base").asText("").trim();
                String quote = node.path("quote").asText("").trim();

                if (!"USDT".equalsIgnoreCase(quote)) {
                    rejectedQuote++;
                    continue;
                }

                if (symbol.isEmpty() || base.isEmpty()) {
                    rejectedAsset++;
                    LOGGER.warn("[SYMBOL-FILTER] REJECT invalid contract: {}", node);
                    continue;
                }

                // This filter uses Bitunix contract metadata and the explicit
                // non-crypto exclusions. It never calls CoinGecko.
                if (!bitunixContractFilter.isAllowedContract(symbol, base)) {
                    rejectedAsset++;
                    continue;
                }

                symbols.add(new TradingSymbol(symbol, base, quote));
            }

            LOGGER.info(
                    "[SYMBOL-FILTER] total={} accepted={} rejectedStatus={} "
                            + "rejectedApi={} rejectedQuote={} rejectedAsset={}",
                    data.size(), symbols.size(), rejectedStatus, rejectedApi,
                    rejectedQuote, rejectedAsset);

            LOGGER.info("[SYMBOL-FILTER] Accepted contracts include XAUTUSDT={}",
                    symbols.stream().anyMatch(
                            s -> "XAUTUSDT".equalsIgnoreCase(s.getExchangeSymbol())));

            return List.copyOf(symbols);

        } catch (ExchangeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ExchangeException("Failed to parse Bitunix trading pairs.", ex);
        }
    }
}
