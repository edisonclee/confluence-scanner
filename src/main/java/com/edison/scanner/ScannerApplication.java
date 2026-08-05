package com.edison.scanner;

import java.util.ArrayList;
import java.util.List;

import com.edison.scanner.bitunix.BitunixSymbolProvider;
import com.edison.scanner.exceptions.ExchangeException;
import com.edison.scanner.market.MarketData;
import com.edison.scanner.market.MarketDataLoader;
import com.edison.scanner.model.ScanResult;
import com.edison.scanner.model.market.TradingSymbol;
import com.edison.scanner.scanner.MarketScanner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Coordinates the complete market scan.
 */
public final class ScannerApplication {

    private final BitunixSymbolProvider symbolProvider;

    private final MarketDataLoader marketDataLoader;

    private final MarketScanner marketScanner;
    
    private final int downloadThreads;

    public ScannerApplication(
            BitunixSymbolProvider symbolProvider,
            MarketDataLoader marketDataLoader,
            MarketScanner marketScanner,
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
    public List<ScanResult> run() {
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

        System.out.println();
        System.out.println("========== Performance ==========");
        System.out.printf("Download : %.2f sec%n",
                downloadSeconds);
        System.out.printf("Scan     : %.2f sec%n",
                scanSeconds);
        System.out.printf("Total    : %.2f sec%n",
                totalSeconds);
        System.out.println("=================================");
        System.out.println();

        return List.copyOf(results);

    }

	public int getDownloadThreads() {
		return downloadThreads;
	}

}