
package com.edison.scanner.bitunix.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.edison.scanner.bitunix.CryptoAssetRegistry;
import com.edison.scanner.exceptions.ExchangeException;
import com.edison.scanner.model.market.TradingSymbol;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public final class BitunixSymbolMapper {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final CryptoAssetRegistry cryptoAssetRegistry;

	public BitunixSymbolMapper(CryptoAssetRegistry cryptoAssetRegistry) {
		this.cryptoAssetRegistry = Objects.requireNonNull(cryptoAssetRegistry);
	}

	public List<TradingSymbol> map(String json) {
		try {
			JsonNode root = OBJECT_MAPPER.readTree(json);
			JsonNode data = root.path("data");

			if (!data.isArray()) {
				throw new ExchangeException("Invalid Bitunix trading-pairs response: data is not an array.");
			}

			List<TradingSymbol> symbols = new ArrayList<>();

			for (JsonNode node : data) {
				if (!isSupported(node)) {
					continue;
				}

				String symbol = node.path("symbol").asText("");
				String base = node.path("base").asText("");
				String quote = node.path("quote").asText("");

				symbols.add(new TradingSymbol(symbol, base, quote));
			}

			return List.copyOf(symbols);

		} catch (ExchangeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ExchangeException("Failed to parse Bitunix trading pairs.", ex);
		}
	}

	private boolean isSupported(JsonNode node) {
		if (!"OPEN".equals(node.path("symbolStatus").asText())) {
			return false;
		}

		if (!node.path("isApiSupported").asBoolean(false)) {
			return false;
		}

		if (!"USDT".equals(node.path("quote").asText())) {
			return false;
		}

		String symbol = node.path("symbol").asText("").trim();
		String base = node.path("base").asText("").trim();

		if (symbol.isEmpty() || base.isEmpty()) {
			return false;
		}

		// Explicit exception requested: retain Tether Gold.
		if ("XAUTUSDT".equalsIgnoreCase(symbol) && "XAUT".equalsIgnoreCase(base)) {
			return true;
		}

		return cryptoAssetRegistry.isCryptoBaseAsset(base);
	}
}
