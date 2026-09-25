package com.edison.scanner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.edison.scanner.bitunix.BitunixSymbolProvider;
import com.edison.scanner.common.Timeframe;
import com.edison.scanner.market.MarketData;
import com.edison.scanner.market.MarketDataCache;
import com.edison.scanner.market.MarketDataLoader;
import com.edison.scanner.model.market.TradingSymbol;
import com.edison.scanner.strategy.impl.TransitionPlayStrategy;
import com.edison.scanner.strategy.transition.TransitionPlayResponse;
import com.edison.scanner.strategy.transition.TransitionPlayResult;

@Component
public class TransitionScannerEngine extends ScannerEngine {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransitionScannerEngine.class);

	private final TransitionPlayStrategy transitionPlayStrategy;

	public TransitionScannerEngine(BitunixSymbolProvider symbolProvider, MarketDataLoader marketDataLoader,
			MarketDataCache cache, TransitionPlayStrategy transitionPlayStrategy, int downloadThreads) {

		super(symbolProvider, marketDataLoader, null, cache, downloadThreads);

		this.transitionPlayStrategy = transitionPlayStrategy;
	}

	public TransitionPlayResponse runTransitionScan() {

		long start = System.nanoTime();

		LOGGER.info("Starting transition scanner: chartTimeframes=[H1, H4]");

		List<TradingSymbol> symbols = symbolProvider.getSymbols();

		List<TransitionPlayResult> results = new ArrayList<>();

		int symbolsScanned = 0;
		int symbolsSkipped = 0;

		for (TradingSymbol symbol : symbols) {

			try {

				LOGGER.debug("Transition scan: symbol={}", symbol.getExchangeSymbol());

				MarketData marketData = new MarketData(symbol);

				/*
				 * Stage 1 Download H1 only.
				 */
				marketDataLoader.load(marketData, EnumSet.of(Timeframe.H1));

				symbolsScanned++;

				/*
				 * RSI filter.
				 */
				if (!transitionPlayStrategy.passesRsi(marketData)) {
					continue;
				}

				/*
				 * Stage 2 Download H4 only.
				 */
				marketDataLoader.load(marketData, EnumSet.of(Timeframe.H4));

				transitionPlayStrategy.scan(marketData).ifPresent(result -> {

					LOGGER.info("Transition signal: symbol={}, direction={}, pattern={}", result.getSymbol(),
							result.getDirection(), result.getPattern());

					results.add(result);
				});

			} catch (Exception ex) {

				symbolsSkipped++;

				/*
				 * Bitunix may still return a symbol from the trading-pairs universe even though
				 * its futures contract is currently not allowed to trade.
				 *
				 * Skip that symbol instead of aborting the entire scan.
				 */
				LOGGER.warn("Skipping transition scan for symbol={} because market data could not be loaded: {}",
						symbol.getExchangeSymbol(), ex.getMessage());
			}
		}

		long end = System.nanoTime();

		double duration = (end - start) / 1_000_000_000.0;

		LOGGER.info("Finished transition scanner: symbolsScanned={}, symbolsSkipped={}, signals={}, totalSeconds={}",
				symbolsScanned, symbolsSkipped, results.size(), duration);

		return new TransitionPlayResponse(LocalDateTime.now(), results, duration, symbolsScanned);
	}
}