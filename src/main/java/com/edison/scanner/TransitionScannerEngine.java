package com.edison.scanner;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

		for (TradingSymbol symbol : symbols) {
			MarketData marketData = new MarketData(symbol);
			/*
			 * Stage 1 Download H1 only.
			 */
			marketDataLoader.load(marketData, java.util.EnumSet.of(Timeframe.H1));

			/*
			 * RSI failed.
			 */
			if (!transitionPlayStrategy.passesRsi(marketData)) {
				continue;
			}

			/*
			 * Stage 2 Download H4 only.
			 */
			marketDataLoader.load(marketData, java.util.EnumSet.of(Timeframe.H4));
			transitionPlayStrategy.scan(marketData).ifPresent(result -> {
				LOGGER.info("Transition signal: symbol={}, direction={}, pattern={}", result.getSymbol(),
						result.getDirection(), result.getPattern());

				results.add(result);
			});
		}

		long end = System.nanoTime();
		double duration = ((end - start) / 1_000_000_000.0);

		LOGGER.info("Finished transition scanner: symbolsScanned={}, signals={}, totalSeconds={}", symbols.size(),
				results.size(), duration);

		return new TransitionPlayResponse(LocalDateTime.now(), results, duration, symbols.size());

	}

}
