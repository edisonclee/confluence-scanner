package com.edison.scanner.oi;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.edison.scanner.oi.model.BinanceOiSnapshot;

@Component
public class BinanceOiRadarClient {

	private static final String BASE_URL = "https://fapi.binance.com";

	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;
	private final CoinGeckoMarketCapClient marketCapClient;

	public BinanceOiRadarClient(CoinGeckoMarketCapClient marketCapClient) {

		this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

		this.objectMapper = new ObjectMapper();

		this.marketCapClient = marketCapClient;
	}

	public List<BinanceOiSnapshot> getSnapshots() {

		try {

			/*
			 * Retrieve market caps ONCE per radar scan.
			 *
			 * Do not call CoinGecko once per coin.
			 */
			Map<String, BigDecimal> marketCaps = marketCapClient.getMarketCaps();

			List<String> symbols = getUsdtSymbols();

			List<BinanceOiSnapshot> results = new ArrayList<>();

			for (String symbol : symbols) {

				try {

					BinanceOiSnapshot snapshot = getSnapshot(symbol, marketCaps);

					if (snapshot != null) {
						results.add(snapshot);
					}

				} catch (Exception ex) {

					System.err.println("Failed to process " + symbol + ": " + ex.getMessage());
				}
			}

			return results;

		} catch (Exception ex) {

			throw new IllegalStateException("Failed to retrieve Binance OI " + "radar data.", ex);
		}
	}

	private List<String> getUsdtSymbols() throws IOException, InterruptedException {

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/fapi/v1/" + "exchangeInfo"))
				.timeout(Duration.ofSeconds(15)).GET().build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {

			throw new IllegalStateException("Binance exchangeInfo returned HTTP " + response.statusCode());
		}

		JsonNode root = objectMapper.readTree(response.body());

		List<String> symbols = new ArrayList<>();

		for (JsonNode symbol : root.get("symbols")) {

			String symbolName = symbol.get("symbol").asText();

			String status = symbol.get("status").asText();

			String quoteAsset = symbol.get("quoteAsset").asText();

			String contractType = symbol.get("contractType").asText();

			if ("TRADING".equals(status) && "USDT".equals(quoteAsset) && "PERPETUAL".equals(contractType)) {

				symbols.add(symbolName);
			}
		}

		return symbols;
	}

	private BinanceOiSnapshot getSnapshot(String symbol, Map<String, BigDecimal> marketCaps)
			throws IOException, InterruptedException {

		/*
		 * Convert BTCUSDT -> BTC
		 */
		String baseSymbol = symbol.endsWith("USDT") ? symbol.substring(0, symbol.length() - 4) : symbol;

		BigDecimal marketCap = marketCaps.get(baseSymbol.toUpperCase());

		/*
		 * If CoinGecko doesn't know the asset, keep the row but mark market cap as
		 * null.
		 */
		BigDecimal price = getPrice(symbol);

		BigDecimal fundingRate = getFundingRate(symbol);

		BigDecimal openInterest = getOpenInterest(symbol);

		List<Candle> candles = getHourlyCandles(symbol);

		if (candles.size() < 5) {
			return null;
		}

		Candle latest = candles.get(candles.size() - 1);

		BigDecimal volume1h = latest.volume();

		BigDecimal volume4h = candles.stream().skip(Math.max(0, candles.size() - 4)).map(Candle::volume)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal priceChange1h = percentageChange(candles.get(candles.size() - 2).close(), latest.close());

		BigDecimal priceChange4h = percentageChange(candles.get(candles.size() - 5).close(), latest.close());

		BigDecimal oiChange1h = getOiChange(symbol, "1h");

		BigDecimal oiChange4h = getOiChange(symbol, "4h");

		return new BinanceOiSnapshot(symbol, price, marketCap, volume1h, volume4h, openInterest, oiChange1h, oiChange4h,
				fundingRate, priceChange1h, priceChange4h);
	}

	private BigDecimal getPrice(String symbol) throws IOException, InterruptedException {

		JsonNode node = get("/fapi/v1/ticker/price" + "?symbol=" + symbol);

		return new BigDecimal(node.get("price").asText());
	}

	private BigDecimal getFundingRate(String symbol) throws IOException, InterruptedException {

		JsonNode node = get("/fapi/v1/premiumIndex" + "?symbol=" + symbol);

		return new BigDecimal(node.get("lastFundingRate").asText());
	}

	private BigDecimal getOpenInterest(String symbol) throws IOException, InterruptedException {

		JsonNode node = get("/fapi/v1/openInterest" + "?symbol=" + symbol);

		return new BigDecimal(node.get("openInterest").asText());
	}

	private BigDecimal getOiChange(String symbol, String period) throws IOException, InterruptedException {

		JsonNode root = get(
				"/futures/data/" + "openInterestHist" + "?symbol=" + symbol + "&period=" + period + "&limit=2");

		if (!root.isArray() || root.size() < 2) {

			return BigDecimal.ZERO;
		}

		JsonNode previous = root.get(root.size() - 2);

		JsonNode latest = root.get(root.size() - 1);

		BigDecimal previousOi = new BigDecimal(previous.get("sumOpenInterest").asText());

		BigDecimal latestOi = new BigDecimal(latest.get("sumOpenInterest").asText());

		if (previousOi.signum() == 0) {
			return BigDecimal.ZERO;
		}

		return latestOi.subtract(previousOi).divide(previousOi, 8, java.math.RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100));
	}

	private List<Candle> getHourlyCandles(String symbol) throws IOException, InterruptedException {

		JsonNode root = get("/fapi/v1/klines" + "?symbol=" + symbol + "&interval=1h" + "&limit=6");

		List<Candle> candles = new ArrayList<>();

		for (JsonNode candle : root) {

			BigDecimal close = new BigDecimal(candle.get(4).asText());

			BigDecimal volume = new BigDecimal(candle.get(5).asText());

			candles.add(new Candle(close, volume));
		}

		return candles;
	}

	private JsonNode get(String path) throws IOException, InterruptedException {

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + path)).timeout(Duration.ofSeconds(15))
				.GET().build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {

			throw new IllegalStateException(
					"Binance API returned HTTP " + response.statusCode() + ": " + response.body());
		}

		return objectMapper.readTree(response.body());
	}

	private BigDecimal percentageChange(BigDecimal oldValue, BigDecimal newValue) {

		if (oldValue == null || oldValue.signum() == 0) {

			return BigDecimal.ZERO;
		}

		return newValue.subtract(oldValue).divide(oldValue, 8, java.math.RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100));
	}

	private record Candle(BigDecimal close, BigDecimal volume) {
	}
}