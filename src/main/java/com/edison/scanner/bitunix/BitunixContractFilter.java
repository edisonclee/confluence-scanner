package com.edison.scanner.bitunix;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Crypto-only Bitunix contract filter.
 *
 * Uses a local allowlist and never calls CoinGecko or any other remote service.
 * Unknown assets are rejected by default.
 */
@Component
public class BitunixContractFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BitunixContractFilter.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // Optional operator-managed override. If absent, use the bundled resource.
    private static final Path EXTERNAL_REGISTRY = Path.of("data", "crypto-assets.json");
    private static final String CLASSPATH_REGISTRY = "/data/crypto-assets.json";

    private static final String XAUT_SYMBOL = "XAUTUSDT";
    private volatile Set<String> cryptoBaseAssets = Set.of();

    public BitunixContractFilter() {
        this.cryptoBaseAssets = loadRegistry();
        LOGGER.info("[CRYPTO-ASSET-REGISTRY] Loaded local crypto base assets={}",
                cryptoBaseAssets.size());
        if (cryptoBaseAssets.isEmpty()) {
            LOGGER.error("[CRYPTO-ASSET-REGISTRY] Registry is empty. All contracts will be rejected except XAUTUSDT.");
        }
    }

    public boolean isAllowedContract(String symbol, String baseAsset) {
        String normalizedSymbol = normalize(symbol);
        String normalizedBase = normalize(baseAsset);

        if (normalizedSymbol.isEmpty() || normalizedBase.isEmpty()) {
            return false;
        }

        if (!normalizedSymbol.endsWith("USDT")
                || !normalizedSymbol.equals(normalizedBase + "USDT")) {
            return false;
        }

        // User-requested exception: tokenized gold is explicitly allowed.
        if (XAUT_SYMBOL.equals(normalizedSymbol) && "XAUT".equals(normalizedBase)) {
            return true;
        }

        boolean allowed = cryptoBaseAssets.contains(normalizedBase);
        if (!allowed) {
            LOGGER.debug("[CRYPTO-ASSET-REGISTRY] Rejected unknown/non-crypto contract={} base={}",
                    normalizedSymbol, normalizedBase);
        }
        return allowed;
    }

    /**
     * Reloads the local registry on demand after an operator updates the JSON.
     * No network call is made. Existing registry remains active if reload fails.
     */
    public synchronized boolean reload() {
        try {
            Set<String> updated = readRegistry();
            if (updated.isEmpty()) {
                LOGGER.warn("[CRYPTO-ASSET-REGISTRY] Reload ignored: registry contained no symbols.");
                return false;
            }
            cryptoBaseAssets = Set.copyOf(updated);
            LOGGER.info("[CRYPTO-ASSET-REGISTRY] Reloaded local crypto base assets={}",
                    cryptoBaseAssets.size());
            return true;
        } catch (Exception ex) {
            LOGGER.error("[CRYPTO-ASSET-REGISTRY] Reload failed; retaining {} existing symbols.",
                    cryptoBaseAssets.size(), ex);
            return false;
        }
    }

    private Set<String> loadRegistry() {
        try {
            Set<String> symbols = readRegistry();
            if (symbols.isEmpty()) {
                throw new IOException("Local crypto asset registry is empty.");
            }
            return Set.copyOf(symbols);
        } catch (Exception ex) {
            LOGGER.error("[CRYPTO-ASSET-REGISTRY] Could not load local registry.", ex);
            return Set.of();
        }
    }

    private Set<String> readRegistry() throws IOException {
        // External file takes precedence, allowing updates without rebuilding the JAR.
        if (Files.isRegularFile(EXTERNAL_REGISTRY)) {
            try (InputStream input = Files.newInputStream(EXTERNAL_REGISTRY)) {
                return parseSymbols(input);
            }
        }

        try (InputStream input = BitunixContractFilter.class.getResourceAsStream(CLASSPATH_REGISTRY)) {
            if (input == null) {
                throw new IOException("Missing classpath registry " + CLASSPATH_REGISTRY);
            }
            return parseSymbols(input);
        }
    }

    private Set<String> parseSymbols(InputStream input) throws IOException {
        JsonNode root = OBJECT_MAPPER.readTree(input);
        JsonNode symbolsNode = root.path("symbols");
        if (!symbolsNode.isArray()) {
            throw new IOException("Registry must contain a symbols array.");
        }

        Set<String> result = new HashSet<>();
        for (JsonNode node : symbolsNode) {
            String symbol = normalize(node.asText(""));
            if (!symbol.isEmpty()) {
                result.add(symbol);
            }
        }
        return result;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
