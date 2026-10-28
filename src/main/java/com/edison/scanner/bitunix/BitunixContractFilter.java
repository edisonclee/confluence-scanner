package com.edison.scanner.bitunix;

import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Filters Bitunix futures contracts for the BB and Transition scanner universe.
 * This class intentionally has no CoinGecko dependency.
 */
@Component
public class BitunixContractFilter {

    /*
     * Exact known non-crypto contracts. Add newly confirmed stock/index
     * contracts here using their full Bitunix contract symbols.
     */
    private static final Set<String> EXCLUDED_CONTRACTS =
            Set.of("PYPLUSDT", "TTWOUSDT", "IWMUSDT");

    public boolean isAllowedContract(String symbol, String baseAsset) {
        String normalizedSymbol = normalize(symbol);
        String normalizedBase = normalize(baseAsset);

        if (normalizedSymbol.isEmpty() || normalizedBase.isEmpty()) {
            return false;
        }

        // Only accept USDT-quoted futures contracts.
        if (!normalizedSymbol.endsWith("USDT")) {
            return false;
        }

        // Guard against mismatched symbol/base metadata.
        if (!normalizedSymbol.equals(normalizedBase + "USDT")) {
            return false;
        }

        // Exclude known stock/index-linked contracts.
        if (EXCLUDED_CONTRACTS.contains(normalizedSymbol)) {
            return false;
        }

        // XAUTUSDT remains allowed. Other candidates must originate from
        // Bitunix's futures contract list before reaching this filter.
        return true;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
