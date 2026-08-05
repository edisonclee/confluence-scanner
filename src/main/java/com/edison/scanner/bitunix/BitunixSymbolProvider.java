package com.edison.scanner.bitunix;

import com.edison.scanner.bitunix.mapper.BitunixSymbolMapper;
import com.edison.scanner.exchange.BinanceFuturesSymbolProvider;
import com.edison.scanner.model.market.TradingSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Provides tradable symbols from Bitunix.
 */
public final class BitunixSymbolProvider {

    private final BitunixClient bitunixClient;

    private final BitunixSymbolMapper mapper;
    
	private final BinanceFuturesSymbolProvider binanceSymbolProvider;
	
	private List<TradingSymbol> symbols;

	public BitunixSymbolProvider(
	        BitunixClient client,
	        BitunixSymbolMapper mapper,
	        BinanceFuturesSymbolProvider
	                binanceSymbolProvider) {

	    this.bitunixClient = Objects.requireNonNull(client);

	    this.mapper = Objects.requireNonNull(mapper);

	    this.binanceSymbolProvider =
	            Objects.requireNonNull(
	                    binanceSymbolProvider);

	}

    /**
     * Returns all supported Bitunix futures symbols.
     */
	public List<TradingSymbol> getSymbols() {

	    if (symbols != null) {
	        return symbols;
	    }

	    List<TradingSymbol> bitunixSymbols =
	            mapper.map(
	                    bitunixClient.getTradingPairs());

	    Set<String> binanceSymbols =
	            binanceSymbolProvider.getSymbols();

	    List<TradingSymbol> filtered =
	            new ArrayList<>();

	    List<String> unsupported =
	            new ArrayList<>();

	    for (TradingSymbol symbol : bitunixSymbols) {

	        if ("XAUTUSDT".equals(
	                symbol.getExchangeSymbol())
	            ||
	            binanceSymbols.contains(
	                    symbol.getExchangeSymbol())) {

	            filtered.add(symbol);

	        } else {

	            unsupported.add(
	                    symbol.getExchangeSymbol());

	        }

	    }

	    System.out.println();
	    System.out.println("========== Symbol Summary ==========");
	    System.out.println("Bitunix Futures : "
	            + bitunixSymbols.size());

	    System.out.println("Binance Futures : "
	            + binanceSymbols.size());

	    System.out.println("Scannable       : "
	            + filtered.size());

	    System.out.println("Unsupported     : "
	            + unsupported.size());

	    System.out.println("====================================");
	    
	    if (!unsupported.isEmpty()) {

	        System.out.println();
	        System.out.println("Symbols not available on Binance Futures:");

	        unsupported.stream()
	                .sorted()
	                .forEach(symbol ->
	                        System.out.println(" - " + symbol));

	        System.out.println();

	    }

	    symbols = List.copyOf(filtered);

	    return symbols;

	}

	public BinanceFuturesSymbolProvider getBinanceSymbolProvider() {
		return binanceSymbolProvider;
	}

}