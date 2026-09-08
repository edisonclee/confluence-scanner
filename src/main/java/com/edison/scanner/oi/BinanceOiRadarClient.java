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

import com.edison.scanner.bitunix.BitunixSymbolProvider;
import com.edison.scanner.model.market.TradingSymbol;
import com.edison.scanner.oi.model.BinanceOiSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class BinanceOiRadarClient {

	private static final String BASE_URL = "https://fapi.binance.com";

	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;
	private final CoinGeckoMarketCapClient marketCapClient;
	private final BitunixSymbolProvider symbolProvider;

	public BinanceOiRadarClient(CoinGeckoMarketCapClient marketCapClient, BitunixSymbolProvider symbolProvider) {

		this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

		this.objectMapper = new ObjectMapper();

		this.marketCapClient = marketCapClient;
		this.symbolProvider = symbolProvider;
	}

	public List<BinanceOiSnapshot> getSnapshots() {

		try {

			/*
			 * Retrieve CoinGecko market caps ONCE per radar scan.
			 *
			 * CoinGecko symbols are normal asset symbols:
			 *
			 * PEPE FLOKI BONK
			 *
			 * Binance may use leveraged/denominated contract symbols:
			 *
			 * 1000PEPEUSDT 1000FLOKIUSDT 1000BONKUSDT
			 *
			 * The normalization happens later when matching the Binance contract to
			 * CoinGecko.
			 */
			Map<String, BigDecimal> marketCaps = marketCapClient.getMarketCaps();

			/*
			 * Bitunix is the source of truth for the universe.
			 *
			 * Binance is only used for market data.
			 */
			List<TradingSymbol> tradingSymbols = symbolProvider.getSymbols();

			List<BinanceOiSnapshot> results = new ArrayList<>();

			for (TradingSymbol tradingSymbol : tradingSymbols) {

				/*
				 * Keep the real Bitunix/Binance contract symbol.
				 *
				 * Example:
				 *
				 * 1000PEPEUSDT
				 */
				String symbol = tradingSymbol.getExchangeSymbol();

				/*
				 * Gold/XAUT is not part of the crypto OI Radar.
				 */
				if (tradingSymbol.isGold()) {
					continue;
				}

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

			throw new IllegalStateException("Failed to retrieve Binance OI radar data.", ex);
		}
	}

	private BinanceOiSnapshot getSnapshot(String symbol, Map<String, BigDecimal> marketCaps)
			throws IOException, InterruptedException {

		/*
		 * Convert the futures contract symbol into the CoinGecko lookup symbol.
		 *
		 * Examples:
		 *
		 * BTCUSDT -> BTC ETHUSDT -> ETH 1000PEPEUSDT -> PEPE 1000FLOKIUSDT -> FLOKI
		 */
		String baseSymbol = extractCoinGeckoSymbol(symbol);

		BigDecimal marketCap = marketCaps.get(baseSymbol.toUpperCase());

		/*
		 * Binance data MUST use the original contract symbol.
		 *
		 * Do NOT use baseSymbol here.
		 */
		BigDecimal price = getPrice(symbol);

		BigDecimal fundingRate = getFundingRate(symbol);

		BigDecimal openInterest = getOpenInterest(symbol);

		List<Candle> candles = getHourlyCandles(symbol);

		/*
		 * Need at least 5 candles:
		 *
		 * candle[-5] -> 4H reference candle[-2] -> 1H reference candle[-1] -> current
		 */
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

	/**
	 * Converts a Binance Futures contract symbol into the corresponding CoinGecko
	 * symbol.
	 *
	 * Examples:
	 *
	 * BTCUSDT -> BTC ETHUSDT -> ETH 1000PEPEUSDT -> PEPE 1000FLOKIUSDT -> FLOKI
	 *
	 * Only the CoinGecko lookup symbol is changed. The original Binance symbol
	 * remains untouched.
	 */
	private String extractCoinGeckoSymbol(String symbol) {

		if (symbol == null || symbol.isBlank()) {
			return "";
		}

		String baseSymbol = symbol.toUpperCase();

		/*
		 * Remove the USDT quote asset.
		 */
		if (baseSymbol.endsWith("USDT")) {

			baseSymbol = baseSymbol.substring(0, baseSymbol.length() - 4);
		}

		/*
		 * Binance uses 1000-prefixed contracts for some tokens.
		 *
		 * Example:
		 *
		 * 1000PEPE -> PEPE 1000FLOKI -> FLOKI
		 */
		if (baseSymbol.startsWith("1000") && baseSymbol.length() > 4) {

			baseSymbol = baseSymbol.substring(4);
		}

		return baseSymbol;
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