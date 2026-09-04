package com.edison.scanner.mapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.edison.scanner.bitunix.model.TickerResponse;
import com.edison.scanner.common.Timeframe;
import com.edison.scanner.config.ApplicationConfig;
import com.edison.scanner.exceptions.ExchangeException;
import com.edison.scanner.model.market.Candle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Bitunix REST client.
 */
public final class BitunixFuturesClient {

	/**
	 * Bitunix maximum candles per request.
	 */
	private static final int MAX_BITUNIX_LIMIT = 200;

	private final ApplicationConfig config;

	private final HttpClient httpClient;

	private final BitunixFuturesCandleMapper candleMapper;

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private Set<String> exchangeSymbols;

	/**
	 * Creates a Bitunix client.
	 *
	 * @param config       application configuration
	 * @param candleMapper Bitunix candle mapper
	 */
	public BitunixFuturesClient(ApplicationConfig config, BitunixFuturesCandleMapper candleMapper) {

		this.config = Objects.requireNonNull(config);
		this.candleMapper = Objects.requireNonNull(candleMapper);
		this.httpClient = HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(15)).build();

	}

	/**
	 * Downloads historical candles.
	 *
	 * Supports requests larger than Bitunix's 1000 candle limit by automatically
	 * paging.
	 *
	 * @param symbol    trading symbol
	 * @param timeframe timeframe
	 * @param limit     total candles requested
	 *
	 * @return candles ordered oldest -> newest
	 */
	public List<Candle> getCandles(String symbol, Timeframe timeframe, int limit) {

		Objects.requireNonNull(symbol, "symbol");
		Objects.requireNonNull(timeframe, "timeframe");

		if (symbol.isBlank()) {
			throw new IllegalArgumentException("symbol must not be blank.");
		}

		if (limit <= 0) {
			throw new IllegalArgumentException("limit must be greater than zero.");
		}

		List<Candle> allCandles = new ArrayList<>();

		Long endTime = null;

		int remaining = limit;

		while (remaining > 0) {

			int requestLimit = Math.min(MAX_BITUNIX_LIMIT, remaining);

			List<Candle> batch = downloadCandles(symbol, timeframe, requestLimit, endTime);

			if (batch.isEmpty()) {
				break;
			}

			/*
			 * Prepend older candles.
			 */
			allCandles.addAll(0, batch);

			remaining -= batch.size();

			/*
			 * Next request ends before the oldest candle already downloaded.
			 */
			endTime = batch.get(0).getOpenTime().toEpochMilli() - 1;

		}

		return List.copyOf(allCandles);

	}

	/**
	 * Downloads one batch from Bitunix.
	 */
	/**
	 * Downloads one batch from Bitunix.
	 */
	private List<Candle> downloadCandles(String symbol, Timeframe timeframe, int limit, Long endTime) {

		URI uri = buildKlineUri(symbol, timeframe.getInterval(), limit, endTime);

		HttpRequest request = HttpRequest.newBuilder(uri).timeout(java.time.Duration.ofSeconds(30))
				.header("User-Agent", "ConfluenceScanner/1.0").header("Accept", "application/json").GET().build();

		HttpResponse<String> response = send(request);

		List<Candle> candles = candleMapper.map(symbol, timeframe, response.body());

		/*
		 * Bitunix returns newest -> oldest. Reverse so we always return oldest ->
		 * newest.
		 */
		java.util.Collections.reverse(candles);

		return candles;

	}

	/**
	 * Builds Bitunix Kline endpoint.
	 */
	private URI buildKlineUri(String symbol, String interval, int limit, Long endTime) {

		StringBuilder url = new StringBuilder(
				String.format("%s/api/v1/futures/market/kline?symbol=%s&interval=%s&limit=%d",
						config.getBitunixBaseUrl(), encode(symbol), encode(interval), Math.min(limit, 200)));

		if (endTime != null) {

			url.append("&endTime=").append(endTime);

		}

		return URI.create(url.toString());

	}

	/**
	 * Validates Bitunix response.
	 */
	/**
	 * Validates Bitunix response.
	 */
	private void validateResponse(HttpResponse<String> response) {

		if (response.statusCode() != 200) {

			throw new ExchangeException("Unexpected response status: " + response.statusCode());

		}

		try {

			JsonNode root = OBJECT_MAPPER.readTree(response.body());

			if (root.get("code").asInt() != 0) {

				throw new ExchangeException("Bitunix error: " + root.get("msg").asText());

			}

		} catch (IOException ex) {

			throw new UncheckedIOException(ex);

		}

	}

	/**
	 * Sends an HTTP request to Bitunix Futures.
	 *
	 * @param request HTTP request
	 *
	 * @return HTTP response
	 */
	private HttpResponse<String> send(HttpRequest request) {

		final int maxRetries = 3;

		for (int attempt = 1; attempt <= maxRetries; attempt++) {

			try {

				HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

				validateResponse(response);

				return response;

			} catch (InterruptedException ex) {

				Thread.currentThread().interrupt();

				throw new ExchangeException("Failed to communicate with Bitunix Futures.", ex);

			} catch (IOException ex) {

				if (attempt == maxRetries) {

					throw new ExchangeException("Failed to communicate with Bitunix Futures.", ex);

				}

				System.err.printf("Bitunix request failed (attempt %d/%d). Retrying...%n", attempt, maxRetries);

				try {

					Thread.sleep(1000);

				} catch (InterruptedException interrupted) {

					Thread.currentThread().interrupt();

					throw new ExchangeException("Retry interrupted.", interrupted);

				}

			}

		}

		throw new ExchangeException("Failed to communicate with Bitunix Futures.");

	}

	/**
	 * URL encodes a value.
	 */
	private String encode(String value) {

		return URLEncoder.encode(value, StandardCharsets.UTF_8);

	}

	public Set<String> getExchangeSymbols() {

		if (exchangeSymbols != null) {
			return exchangeSymbols;
		}

		HttpRequest request = HttpRequest.newBuilder(buildExchangeInfoUri()).timeout(java.time.Duration.ofSeconds(30))
				.header("User-Agent", "ConfluenceScanner/1.0").header("Accept", "application/json").GET().build();

		HttpResponse<String> response = send(request);

		Set<String> symbols = new HashSet<>();

		try {

			JsonNode root = OBJECT_MAPPER.readTree(response.body());

			for (JsonNode symbol : root.get("data")) {

				if (!"OPEN".equals(symbol.get("symbolStatus").asText())) {
					continue;
				}

				if (!symbol.get("isApiSupported").asBoolean()) {
					continue;
				}

				symbols.add(symbol.get("symbol").asText());

			}

		} catch (IOException ex) {

			throw new UncheckedIOException(ex);

		}

		exchangeSymbols = Set.copyOf(symbols);

		return exchangeSymbols;

	}

	private URI buildExchangeInfoUri() {

		return URI.create(config.getBitunixBaseUrl() + "/api/v1/futures/market/trading_pairs");

	}

	public void setExchangeSymbols(Set<String> exchangeSymbols) {
		this.exchangeSymbols = exchangeSymbols;
	}

	public BigDecimal getCurrentPrice(String symbol) {

		URI uri = URI.create(config.getBitunixBaseUrl() + "/api/v1/futures/market/tickers?symbols=" + encode(symbol));

		HttpRequest request = HttpRequest.newBuilder(uri).timeout(java.time.Duration.ofSeconds(30))
				.header("User-Agent", "ConfluenceScanner/1.0").header("Accept", "application/json").GET().build();

		HttpResponse<String> response = send(request);

		try {

			TickerResponse ticker = OBJECT_MAPPER.readValue(response.body(), TickerResponse.class);

			if (ticker.getData() == null || ticker.getData().isEmpty()) {

				throw new ExchangeException("Ticker not found: " + symbol);
			}

			return ticker.getData().get(0).getLastPrice();

		} catch (IOException ex) {

			throw new UncheckedIOException(ex);

		}

	}
	
}