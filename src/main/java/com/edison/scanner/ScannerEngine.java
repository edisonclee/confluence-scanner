package com.edison.scanner;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edison.scanner.bitunix.BitunixSymbolProvider;
import com.edison.scanner.common.Timeframe;
import com.edison.scanner.exceptions.ExchangeException;
import com.edison.scanner.market.MarketData;
import com.edison.scanner.market.MarketDataCache;
import com.edison.scanner.market.MarketDataLoader;
import com.edison.scanner.model.ScanExecutionResult;
import com.edison.scanner.model.ScanResult;
import com.edison.scanner.model.ScannerRequest;
import com.edison.scanner.model.market.TradingSymbol;
import com.edison.scanner.strategy.ScannerStrategy;

/**
 * Coordinates the complete market scan.
 */
public class ScannerEngine {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScannerEngine.class);

	protected final BitunixSymbolProvider symbolProvider;

	protected final MarketDataLoader marketDataLoader;

	protected final ScannerStrategy marketScanner;

	protected final int downloadThreads;

	protected final MarketDataCache cache;

	public ScannerEngine(BitunixSymbolProvider symbolProvider, MarketDataLoader marketDataLoader,
			ScannerStrategy marketScanner, MarketDataCache cache, int downloadThreads) {

		this.symbolProvider = Objects.requireNonNull(symbolProvider);
		this.marketDataLoader = Objects.requireNonNull(marketDataLoader);
		this.marketScanner = marketScanner;
		this.cache = Objects.requireNonNull(cache);
		this.downloadThreads = downloadThreads;

	}

	protected ScanExecutionResult runScanner(ScannerRequest request) {

		Objects.requireNonNull(request, "request");

		String scannerName = Objects.requireNonNull(marketScanner, "marketScanner").getName();

		LOGGER.info("Starting scanner: strategy={}, bbTimeframes={}", scannerName, request.getBbTimeframes());

		long totalStart = System.nanoTime();

		List<TradingSymbol> symbols = preloadMarketData(request);

		long downloadEnd = System.nanoTime();

		double downloadSeconds = (downloadEnd - totalStart) / 1_000_000_000.0;

		List<ScanResult> results = new ArrayList<>();

		for (TradingSymbol symbol : symbols) {

			MarketData marketData = marketDataLoader.get(symbol);

			if (marketData == null) {
				continue;
			}

			results.addAll(marketScanner.scan(marketData, request));

		}

		long totalEnd = System.nanoTime();

		double scanSeconds = (totalEnd - downloadEnd) / 1_000_000_000.0;

		double totalSeconds = (totalEnd - totalStart) / 1_000_000_000.0;

		LOGGER.info(
				"Finished scanner: strategy={}, symbolsScanned={}, matches={}, downloadSeconds={}, scanSeconds={}, totalSeconds={}",
				scannerName, symbols.size(), results.size(), downloadSeconds, scanSeconds, totalSeconds);

		return new ScanExecutionResult(results, symbols.size(), downloadSeconds, scanSeconds, totalSeconds);

	}

	protected List<TradingSymbol> preloadMarketData(ScannerRequest request) {

		cache.clear();

		List<TradingSymbol> symbols = symbolProvider.getSymbols();

		Set<Timeframe> requiredTimeframes = EnumSet.of(Timeframe.H1);

		requiredTimeframes.addAll(request.getBbTimeframes());

		ExecutorService executor = Executors.newFixedThreadPool(downloadThreads);

		List<Future<?>> futures = new ArrayList<>();

		for (TradingSymbol symbol : symbols) {

			futures.add(executor.submit(() -> {

				try {

					marketDataLoader.preload(symbol, requiredTimeframes);

				} catch (Exception ex) {

					LOGGER.error("Failed to download market data: symbol={}", symbol.getExchangeSymbol(), ex);

				}

			}));

		}

		for (Future<?> future : futures) {

			try {

				future.get();

			} catch (Exception ex) {

				throw new ExchangeException("Failed while downloading market data.", ex);

			}

		}

		executor.shutdown();

		return symbols;

	}

	public ScanExecutionResult run(ScannerRequest request) {

		return runScanner(request);

	}

	public int getDownloadThreads() {
		return downloadThreads;
	}

	public MarketDataCache getCache() {
		return cache;
	}

}
