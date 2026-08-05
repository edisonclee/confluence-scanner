package com.edison.scanner.market;

import com.edison.scanner.model.market.TradingSymbol;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores downloaded market data.
 */
public final class MarketDataCache {

    /**
     * Cached market data.
     */
    private final Map<TradingSymbol, MarketData> cache =
            new ConcurrentHashMap<>();

    /**
     * Stores market data.
     *
     * @param marketData market data
     */
    public void put(
            MarketData marketData) {

        cache.put(
                marketData.getSymbol(),
                marketData);

    }

    /**
     * Returns cached market data.
     *
     * @param symbol trading symbol
     *
     * @return market data
     */
    public MarketData get(
            TradingSymbol symbol) {

        return cache.get(symbol);

    }

    /**
     * Returns all cached market data.
     *
     * @return cached market data
     */
    public Collection<MarketData> values() {

        return cache.values();

    }

    /**
     * Returns whether the symbol is cached.
     *
     * @param symbol trading symbol
     *
     * @return true if cached
     */
    public boolean contains(
            TradingSymbol symbol) {

        return cache.containsKey(symbol);

    }

    /**
     * Clears the cache.
     */
    public void clear() {

        cache.clear();

    }

}