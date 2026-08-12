package com.edison.scanner;

import java.util.ArrayList;
import java.util.List;

import com.edison.scanner.bitunix.BitunixSymbolProvider;
import com.edison.scanner.exceptions.ExchangeException;
import com.edison.scanner.market.MarketData;
import com.edison.scanner.market.MarketDataLoader;
import com.edison.scanner.model.ScanExecutionResult;
import com.edison.scanner.model.ScanResult;
import com.edison.scanner.model.market.TradingSymbol;
import com.edison.scanner.strategy.impl.BollingerBandStrategy;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Coordinates the complete market scan.
 */
public final class ScannerEngine {

    private final BitunixSymbolProvider symbolProvider;

    private final MarketDataLoader marketDataLoader;

    private final BollingerBandStrategy marketScanner;
    
    private final int downloadThreads;

    public ScannerEngine(
            BitunixSymbolProvider symbolProvider,
            MarketDataLoader marketDataLoader,
            BollingerBandStrategy marketScanner,
            int downloadThreads) {

        this.symbolProvider = symbolProvider;
        this.marketDataLoader = marketDataLoader;
        this.marketScanner = marketScanner;
        this.downloadThreads = downloadThreads;

    }

    /**
     * Executes the complete scan.
     *
     * @return scan results
     */
    public ScanExecutionResult run() {
    	long totalStart = System.nanoTime();

        List<TradingSymbol> symbols =
                symbolProvider.getSymbols();

        /*
         * Phase 1
         * Download everything.
         */
        ExecutorService executor =
                Executors.newFixedThreadPool(
                        downloadThreads);

        List<Future<?>> futures =
                new ArrayList<>();

        for (TradingSymbol symbol : symbols) {

            futures.add(
                    executor.submit(() -> {

                        try {

                            marketDataLoader.preload(symbol);

                        } catch (Exception ex) {

                            System.err.printf(
                                    "Failed to download %s : %s%n",
                                    symbol.getExchangeSymbol(),
                                    ex.getMessage());

                        }

                    }));

        }

        for (Future<?> future : futures) {

            try {

                future.get();

            } catch (Exception ex) {

                throw new ExchangeException(
                        "Failed while downloading market data.",
                        ex);

            }

        }

        executor.shutdown();
        
        long downloadEnd = System.nanoTime();

        double downloadSeconds =
                (downloadEnd - totalStart)
                / 1_000_000_000.0;

        /*
         * Phase 2
         * Scan everything.
         */
        List<ScanResult> results =
                new ArrayList<>();

        for (TradingSymbol symbol : symbols) {

            MarketData marketData =
                    marketDataLoader.get(symbol);

            if (marketData == null) {
                continue;
            }

            results.addAll(
                    marketScanner.scan(
                            marketData));

        }
        
        long totalEnd = System.nanoTime();

        double scanSeconds =
                (totalEnd - downloadEnd)
                / 1_000_000_000.0;

        double totalSeconds =
                (totalEnd - totalStart)
                / 1_000_000_000.0;


        return new ScanExecutionResult(
                results,
                symbols.size(),
                downloadSeconds,
                scanSeconds,
                totalSeconds);

    }

	public int getDownloadThreads() {
		return downloadThreads;
	}

}