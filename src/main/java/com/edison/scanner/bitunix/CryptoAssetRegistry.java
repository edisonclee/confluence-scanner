
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

    private static final Logger LOGGER =
            LoggerFactory.getLogger(CryptoAssetRegistry.class);

    private static final URI COINGECKO_URI = URI.create(
            "https://api.coingecko.com/api/v3/coins/list?include_platform=false");

    private static final Path CACHE_PATH =
            Path.of("data", "coingecko-coins.json");

    private static final long REFRESH_SECONDS = 24 * 60 * 60;
    private static final long RETRY_SECONDS = 6 * 60 * 60;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private volatile Set<String> cryptoSymbols = Set.of();
    private volatile Instant lastSuccessfulRefresh = Instant.EPOCH;
    private volatile Instant nextRefreshAttempt = Instant.EPOCH;

    public CryptoAssetRegistry() {
        loadCache();
    }

    public boolean isCryptoBaseAsset(String baseAsset) {
        if (baseAsset == null || baseAsset.isBlank()) {
            return false;
        }

        refreshIfNeeded();

        return cryptoSymbols.contains(
                baseAsset.trim().toUpperCase(Locale.ROOT));
    }

    private synchronized void refreshIfNeeded() {
        Instant now = Instant.now();

        if (now.isBefore(nextRefreshAttempt)) {
            return;
        }

        if (now.getEpochSecond()
                - lastSuccessfulRefresh.getEpochSecond() < REFRESH_SECONDS) {
            return;
        }

        try {
            refreshFromCoinGecko();

            lastSuccessfulRefresh = Instant.now();
            nextRefreshAttempt =
                    lastSuccessfulRefresh.plusSeconds(REFRESH_SECONDS);

            saveCache();

            LOGGER.info("Crypto registry refreshed. Symbols={}",
                    cryptoSymbols.size());

        } catch (Exception ex) {
            nextRefreshAttempt = Instant.now().plusSeconds(RETRY_SECONDS);

            LOGGER.warn(
                    "CoinGecko refresh failed. Keeping {} cached symbols. "
                    + "Next refresh attempt after {}. Cause: {}",
                    cryptoSymbols.size(),
                    nextRefreshAttempt,
                    ex.getMessage());
        }
    }

    private void refreshFromCoinGecko()
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder(COINGECKO_URI)
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException(
                    "CoinGecko returned HTTP " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());

        if (!root.isArray() || root.isEmpty()) {
            throw new IOException(
                    "CoinGecko returned an empty or invalid coin list.");
        }

        Set<String> symbols = new HashSet<>();

        for (JsonNode coin : root) {
            String symbol = coin.path("symbol").asText("").trim();

            if (!symbol.isEmpty()) {
                symbols.add(symbol.toUpperCase(Locale.ROOT));
            }
        }

        if (symbols.isEmpty()) {
            throw new IOException(
                    "No symbols found in CoinGecko response.");
        }

        // Replace the cache only after a valid response is received.
        cryptoSymbols = Set.copyOf(symbols);
    }

    private void loadCache() {
        if (!Files.exists(CACHE_PATH)) {
            LOGGER.warn(
                    "Crypto registry cache not found: {}. "
                    + "A successful CoinGecko refresh is required "
                    + "to identify crypto assets.",
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
                String symbol = node.asText("").trim();

                if (!symbol.isEmpty()) {
                    symbols.add(symbol.toUpperCase(Locale.ROOT));
                }
            }

            if (symbols.isEmpty()) {
                throw new IOException("Crypto registry cache is empty.");
            }

            cryptoSymbols = Set.copyOf(symbols);

            lastSuccessfulRefresh = Instant.ofEpochSecond(
                    root.path("lastRefreshEpoch").asLong(0));

            LOGGER.info("Loaded cached crypto registry. Symbols={}",
                    cryptoSymbols.size());

        } catch (Exception ex) {
            cryptoSymbols = Set.of();
            lastSuccessfulRefresh = Instant.EPOCH;

            LOGGER.error("Could not load crypto registry cache.", ex);
        }
    }

    private void saveCache() throws IOException {
        Files.createDirectories(CACHE_PATH.getParent());

        ObjectNode root = objectMapper.createObjectNode();
        root.put("lastRefreshEpoch",
                lastSuccessfulRefresh.getEpochSecond());

        ArrayNode symbols = root.putArray("symbols");
        cryptoSymbols.stream().sorted().forEach(symbols::add);

        try (var output = Files.newOutputStream(
                CACHE_PATH,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(output, root);
        }
    }
}
