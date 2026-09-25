package com.edison.scanner.bitunix;

import java.util.List;
import java.util.Objects;

import com.edison.scanner.model.market.TradingSymbol;

/**
 * Provides the shared eligible Bitunix Futures universe.
 */
public final class BitunixSymbolProvider {

	private final BitunixUniverseService universeService;

	public BitunixSymbolProvider(BitunixUniverseService universeService) {

		this.universeService = Objects.requireNonNull(universeService);
	}

	public List<TradingSymbol> getSymbols() {

		return universeService.getEligibleSymbols();
	}
}