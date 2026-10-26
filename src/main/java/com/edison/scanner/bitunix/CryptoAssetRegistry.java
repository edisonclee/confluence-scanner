
package com.edison.scanner.bitunix;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class CryptoAssetRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(CryptoAssetRegistry.class);

	private static final URI COINGECKO_URI = URI
			.create("https://api.coingecko.com/api/v3/coins/list?include_platform=false");

	private static final Path CACHE_PATH = Path.of("data", "coingecko-coins.json");

	private static final long REFRESH_SECONDS = 24 * 60 * 60;
	private static final long RETRY_SECONDS = 6 * 60 * 60;

	/*
	 * Explicit exclusions for confirmed non-crypto contracts.
	 *
	 * Add more exact Bitunix contract symbols here as you identify them. Matching
	 * the full contract avoids excluding an unrelated contract solely because it
	 * shares a ticker.
	 */
	private static final Set<String> EXCLUDED_CONTRACTS = Set.of("PYPLUSDT", "TTWOUSDT", "IWMUSDT");

	/*
	 * Explicit exception requested by the user. XAUT is Tether Gold, a tokenized
	 * gold asset.
	 */
	private static final String EXCEPTION_SYMBOL = "XAUTUSDT";
	private static final String EXCEPTION_BASE = "XAUT";

	private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

	private final ObjectMapper objectMapper = new ObjectMapper();

	private volatile Set<String> cryptoSymbols = Set.of();
	private volatile Instant lastSuccessfulRefresh = Instant.EPOCH;
	private volatile Instant nextRefreshAttempt = Instant.EPOCH;

	public CryptoAssetRegistry() {
		loadCache();
	}

	/**
	 * Determines whether a Bitunix contract should enter the scanner universe.
	 *
	 * Explicit contract exclusions are checked before CoinGecko ticker matching.
	 */
	public boolean isAllowedContract(String symbol, String baseAsset) {
		String normalizedSymbol = normalize(symbol);
		String normalizedBase = normalize(baseAsset);

		if (normalizedSymbol.isEmpty() || normalizedBase.isEmpty()) {
			LOGGER.debug("[ASSET-FILTER] REJECT symbol={} base={} reason=EMPTY_SYMBOL_OR_BASE", symbol, baseAsset);
			return false;
		}

		// Always retain the explicitly requested XAUTUSDT contract.
		if (EXCEPTION_SYMBOL.equals(normalizedSymbol) && EXCEPTION_BASE.equals(normalizedBase)) {
			LOGGER.info("[ASSET-FILTER] ACCEPT symbol={} base={} reason=EXPLICIT_EXCEPTION", normalizedSymbol,
					normalizedBase);
			return true;
		}

		// Reject exact known non-crypto contracts.
		if (EXCLUDED_CONTRACTS.contains(normalizedSymbol)) {
			LOGGER.info("[ASSET-FILTER] REJECT symbol={} base={} reason=EXPLICIT_EXCLUSION", normalizedSymbol,
					normalizedBase);
			return false;
		}

		boolean crypto = isCryptoBaseAsset(normalizedBase);

		if (!crypto) {
			LOGGER.info("[ASSET-FILTER] REJECT symbol={} base={} reason=NOT_IN_COINGECKO_SYMBOL_LIST", normalizedSymbol,
					normalizedBase);
			return false;
		}

		LOGGER.debug("[ASSET-FILTER] ACCEPT symbol={} base={} reason=COINGECKO_SYMBOL_MATCH", normalizedSymbol,
				normalizedBase);

		return true;
	}

	/**
	 * Checks whether a base ticker appears in CoinGecko's symbol list.
	 *
	 * Note: this is a ticker lookup, not proof that a specific exchange contract
	 * represents the corresponding crypto asset.
	 */
	public boolean isCryptoBaseAsset(String baseAsset) {
		if (baseAsset == null || baseAsset.isBlank()) {
			return false;
		}

		refreshIfNeeded();

		return cryptoSymbols.contains(normalize(baseAsset));
	}

	private synchronized void refreshIfNeeded() {
		Instant now = Instant.now();

		if (now.isBefore(nextRefreshAttempt)) {
			return;
		}

		if (now.getEpochSecond() - lastSuccessfulRefresh.getEpochSecond() < REFRESH_SECONDS) {
			return;
		}

		try {
			refreshFromCoinGecko();

			lastSuccessfulRefresh = Instant.now();
			nextRefreshAttempt = lastSuccessfulRefresh.plusSeconds(REFRESH_SECONDS);

			saveCache();

			LOGGER.info("[CRYPTO-REGISTRY] Refreshed successfully. Symbols={}", cryptoSymbols.size());

		} catch (Exception ex) {
			nextRefreshAttempt = Instant.now().plusSeconds(RETRY_SECONDS);

			LOGGER.warn(
					"[CRYPTO-REGISTRY] Refresh failed. Keeping {} cached symbols. " + "Next attempt after {}. Cause={}",
					cryptoSymbols.size(), nextRefreshAttempt, ex.getMessage());
		}
	}

	private void refreshFromCoinGecko() throws IOException, InterruptedException {

		HttpRequest request = HttpRequest.newBuilder(COINGECKO_URI).timeout(Duration.ofSeconds(30))
				.header("Accept", "application/json").GET().build();

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
			String symbol = normalize(coin.path("symbol").asText(""));

			if (!symbol.isEmpty()) {
				symbols.add(symbol);
			}
		}

		if (symbols.isEmpty()) {
			throw new IOException("No symbols found in CoinGecko response.");
		}

		// Replace the cache only after receiving a valid response.
		cryptoSymbols = Set.copyOf(symbols);
	}

	private void loadCache() {
		if (!Files.exists(CACHE_PATH)) {
			LOGGER.warn("[CRYPTO-REGISTRY] Cache not found: {}. " + "A successful CoinGecko refresh is required.",
					CACHE_PATH);
			return;
		}

		try {
			JsonNode root = objectMapper.readTree(CACHE_PATH.toFile());
			JsonNode symbolsNode = root.path("symbols");

			if (!symbolsNode.isArray()) {
				throw new IOException("Invalid crypto registry cache.");
			}

			Set<String> symbols = new HashSet<>();

			for (JsonNode node : symbolsNode) {
				String symbol = normalize(node.asText(""));

				if (!symbol.isEmpty()) {
					symbols.add(symbol);
				}
			}

			if (symbols.isEmpty()) {
				throw new IOException("Crypto registry cache is empty.");
			}

			cryptoSymbols = Set.copyOf(symbols);

			lastSuccessfulRefresh = Instant.ofEpochSecond(root.path("lastRefreshEpoch").asLong(0));

			LOGGER.info("[CRYPTO-REGISTRY] Loaded cached symbols={}", cryptoSymbols.size());

		} catch (Exception ex) {
			cryptoSymbols = Set.of();
			lastSuccessfulRefresh = Instant.EPOCH;

			LOGGER.error("[CRYPTO-REGISTRY] Could not load cache.", ex);
		}
	}

	private void saveCache() throws IOException {
		Files.createDirectories(CACHE_PATH.getParent());

		ObjectNode root = objectMapper.createObjectNode();
		root.put("lastRefreshEpoch", lastSuccessfulRefresh.getEpochSecond());

		ArrayNode symbols = root.putArray("symbols");

		cryptoSymbols.stream().sorted().forEach(symbols::add);

		try (var output = Files.newOutputStream(CACHE_PATH, StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {

			objectMapper.writerWithDefaultPrettyPrinter().writeValue(output, root);
		}
	}

	private static String normalize(String value) {
		return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
	}
}
