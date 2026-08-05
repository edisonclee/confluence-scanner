package com.edison.scanner.model.market;

import java.util.Objects;

/**
 * Represents a tradable futures symbol.
 */
public final class TradingSymbol {

    /**
     * Exchange symbol.
     *
     * Example:
     * BTCUSDT
     */
    private final String exchangeSymbol;

    /**
     * TradingView symbol.
     *
     * Example:
     * BTCUSDT.P
     */
    private final String tradingViewSymbol;

    /**
     * Base asset.
     *
     * Example:
     * BTC
     */
    private final String baseAsset;

    /**
     * Quote asset.
     *
     * Example:
     * USDT
     */
    private final String quoteAsset;
    
    public boolean isGold() {
        return exchangeSymbol.equals("XAUTUSDT");
    }

    public TradingSymbol(
            String exchangeSymbol,
            String baseAsset,
            String quoteAsset) {

        this.exchangeSymbol =
                Objects.requireNonNull(exchangeSymbol);

        this.baseAsset =
                Objects.requireNonNull(baseAsset);

        this.quoteAsset =
                Objects.requireNonNull(quoteAsset);

        this.tradingViewSymbol =
                exchangeSymbol + ".P";
    }

    public String getExchangeSymbol() {
        return exchangeSymbol;
    }

    public String getTradingViewSymbol() {
        return tradingViewSymbol;
    }

    public String getBaseAsset() {
        return baseAsset;
    }

    public String getQuoteAsset() {
        return quoteAsset;
    }

    @Override
    public String toString() {
        return exchangeSymbol;
    }

}