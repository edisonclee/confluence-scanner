package com.edison.scanner.oi;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CoinGeckoMarketCapClient {

	private static final String BASE_URL = "https://api.coingecko.com/api/v3";

	/*
	 * We only need market cap.
	 *
	 * 250 coins per page. Four pages gives us up to 1,000 coins.
	 *
	 * This is enough to cover the majority of Binance perpetual contracts while
	 * keeping API usage low.
	 */
	private static final int PAGE_SIZE = 250;
	private static final int MAX_PAGES = 4;

	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;

	public CoinGeckoMarketCapClient() {

		this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

		this.objectMapper = new ObjectMapper();
	}

	public Map<String, BigDecimal> getMarketCaps() {

		Map<String, BigDecimal> marketCaps = new HashMap<>();

		try {

			for (int page = 1; page <= MAX_PAGES; page++) {

				JsonNode response = get("/coins/markets" + "?vs_currency=usd" + "&order=market_cap_desc" + "&per_page="
						+ PAGE_SIZE + "&page=" + page + "&sparkline=false");

				if (!response.isArray() || response.isEmpty()) {
					break;
				}

				for (JsonNode coin : response) {

					JsonNode symbolNode = coin.get("symbol");

					JsonNode marketCapNode = coin.get("market_cap");

					if (symbolNode == null || marketCapNode == null || marketCapNode.isNull()) {
						continue;
					}

					String symbol = symbolNode.asText().toUpperCase();

					BigDecimal marketCap = new BigDecimal(marketCapNode.asText());

					/*
					 * If CoinGecko has multiple assets with the same symbol, the first occurrence
					 * is normally the higher-ranked asset.
					 *
					 * Do not overwrite it.
					 */
					marketCaps.putIfAbsent(symbol, marketCap);
				}
			}

			return marketCaps;

		} catch (Exception ex) {

			throw new IllegalStateException("Failed to retrieve market-cap data " + "from CoinGecko.", ex);
		}
	}

	private JsonNode get(String path) throws IOException, InterruptedException {

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + path)).timeout(Duration.ofSeconds(20))
				.GET().build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {

			throw new IllegalStateException(
					"CoinGecko API returned HTTP " + response.statusCode() + ": " + response.body());
		}

		return objectMapper.readTree(response.body());
	}
}