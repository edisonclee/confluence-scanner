package com.edison.scanner.bitunix.model;

import java.time.LocalDate;

public record BitunixUniverseEntry(
        String symbol,
        String baseAsset,
        String quoteAsset,
        LocalDate firstSeen) {
}