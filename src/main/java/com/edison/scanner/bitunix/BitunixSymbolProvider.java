package com.edison.scanner.bitunix;

import java.util.List;
import java.util.Objects;

import com.edison.scanner.bitunix.mapper.BitunixSymbolMapper;
import com.edison.scanner.model.market.TradingSymbol;

/**
 * Provides tradable symbols from Bitunix.
 */
public final class BitunixSymbolProvider {

    private final BitunixClient bitunixClient;

    private final BitunixSymbolMapper mapper;
    
	private List<TradingSymbol> symbols;

	public BitunixSymbolProvider(
	        BitunixClient client,
	        BitunixSymbolMapper mapper) {

	    this.bitunixClient = Objects.requireNonNull(client);
	    this.mapper = Objects.requireNonNull(mapper);
	}

    /**
     * Returns all supported Bitunix futures symbols.
     */
	public List<TradingSymbol> getSymbols() {

	    if (symbols != null) {
	        return symbols;
	    }

	    symbols = mapper.map(
	            bitunixClient.getTradingPairs());

	    return symbols;
	}


}