
package com.edison.scanner.bitunix;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Maintains a cached allowlist of crypto asset symbols from CoinGecko. Unknown
 * symbols are rejected. XAUTUSDT is handled separately.
 */
@Component
public class CryptoAssetRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(CryptoAssetRegistry.class);

	private static final URI COINGECKO_URI = URI
			.create("https://api.coingecko.com/api/v3/coins/list?include_platform=false");

	private static final Path CACHE_PATH = Path.of("data", "coingecko-coins.json");

	private static final long REFRESH_SECONDS = 24 * 60 * 60;

	private final HttpClient httpClient = HttpClient.newHttpClient();
	private final ObjectMapper objectMapper = new ObjectMapper();

	private final AtomicBoolean refreshing = new AtomicBoolean(false);

	private volatile Set<String> cryptoSymbols = Set.of();
	private volatile Instant lastRefresh = Instant.EPOCH;

	public CryptoAssetRegistry() {
		loadCache();
	}

	public boolean isCryptoBaseAsset(String baseAsset) {
		if (baseAsset == null || baseAsset.isBlank()) {
			return false;
		}

		refreshIfNeeded();

		return cryptoSymbols.contains(baseAsset.trim().toUpperCase(Locale.ROOT));
	}

	private void refreshIfNeeded() {
		if (Instant.now().getEpochSecond() - lastRefresh.getEpochSecond() < REFRESH_SECONDS) {
			return;
		}

		if (!refreshing.compareAndSet(false, true)) {
			return;
		}

		try {
			refreshFromCoinGecko();
		} catch (Exception ex) {
			LOGGER.warn("Unable to refresh CoinGecko asset registry. "
					+ "Using cached registry; unknown assets remain excluded.", ex);
		} finally {
			refreshing.set(false);
		}
	}

	private void refreshFromCoinGecko() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(COINGECKO_URI).header("Accept", "application/json").GET().build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			throw new IOException("CoinGecko returned HTTP " + response.statusCode());
		}

		JsonNode root = objectMapper.readTree(response.body());

		if (!root.isArray() || root.isEmpty()) {
			throw new IOException("CoinGecko returned an empty or invalid coin list.");
		}

		Set<String> symbols = new HashSet<>();

		for (JsonNode coin : root) {
			String symbol = coin.path("symbol").asText("").trim();

			if (!symbol.isEmpty()) {
				symbols.add(symbol.toUpperCase(Locale.ROOT));
			}
		}

		if (symbols.isEmpty()) {
			throw new IOException("No symbols found in CoinGecko response.");
		}

		cryptoSymbols = Set.copyOf(symbols);
		lastRefresh = Instant.now();

		saveCache();

		LOGGER.info("CoinGecko crypto registry refreshed. Symbols={}", cryptoSymbols.size());
	}

	private void loadCache() {
		if (!Files.exists(CACHE_PATH)) {
			LOGGER.warn("CoinGecko cache not found at {}. " + "Only explicitly allowed assets can pass until "
					+ "the registry is successfully downloaded.", CACHE_PATH);
			return;
		}

		try {
			JsonNode root = objectMapper.readTree(CACHE_PATH.toFile());
			JsonNode symbolsNode = root.path("symbols");

			if (!symbolsNode.isArray()) {
				throw new IOException("Invalid CoinGecko cache format.");
			}

			Set<String> symbols = new HashSet<>();

			for (JsonNode node : symbolsNode) {
				String symbol = node.asText("").trim();

				if (!symbol.isEmpty()) {
					symbols.add(symbol.toUpperCase(Locale.ROOT));
				}
			}

			if (symbols.isEmpty()) {
				throw new IOException("CoinGecko cache contains no symbols.");
			}

			cryptoSymbols = Set.copyOf(symbols);
			lastRefresh = Instant.ofEpochSecond(root.path("lastRefreshEpoch").asLong(0));

			LOGGER.info("Loaded CoinGecko crypto registry. Symbols={}", cryptoSymbols.size());

		} catch (Exception ex) {
			cryptoSymbols = Set.of();
			lastRefresh = Instant.EPOCH;

			LOGGER.error("Failed to load CoinGecko cache.", ex);
		}
	}

	private void saveCache() throws IOException {
		Files.createDirectories(CACHE_PATH.getParent());

		ObjectNode root = objectMapper.createObjectNode();
		root.put("lastRefreshEpoch", lastRefresh.getEpochSecond());

		ArrayNode symbols = root.putArray("symbols");
		cryptoSymbols.stream().sorted().forEach(symbols::add);

		objectMapper.writerWithDefaultPrettyPrinter().writeValue(Files.newOutputStream(CACHE_PATH,
				StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE), root);
	}
}
