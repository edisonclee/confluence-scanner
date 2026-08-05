package com.edison.scanner.exchange;

import java.util.Objects;
import java.util.Set;

/**
 * Provides all Binance Futures trading symbols.
 */
public final class BinanceFuturesSymbolProvider {

    private final BinanceFuturesClient client;

    public BinanceFuturesSymbolProvider(
            BinanceFuturesClient client) {

        this.client = Objects.requireNonNull(client);

    }

    /**
     * Returns all Binance Futures symbols.
     *
     * @return exchange symbols
     */
    public Set<String> getSymbols() {

        return client.getExchangeSymbols();

    }

}