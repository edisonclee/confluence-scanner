package com.edison.scanner;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.edison.scanner.bitunix.BitunixClient;
import com.edison.scanner.bitunix.BitunixSymbolProvider;
import com.edison.scanner.bitunix.mapper.BitunixSymbolMapper;
import com.edison.scanner.common.Timeframe;
import com.edison.scanner.config.ApplicationConfig;
import com.edison.scanner.converter.HeikenAshiConverter;
import com.edison.scanner.detector.TouchDetector;
import com.edison.scanner.exchange.BinanceFuturesClient;
import com.edison.scanner.exchange.BinanceFuturesSymbolProvider;
import com.edison.scanner.indicator.BollingerBandCalculator;
import com.edison.scanner.mapper.BinanceFuturesCandleMapper;
import com.edison.scanner.market.MarketDataCache;
import com.edison.scanner.market.MarketDataLoader;
import com.edison.scanner.model.ScanResult;
import com.edison.scanner.scanner.MarketScanner;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {

        System.out.println("=========================================");
        System.out.println("   Bollinger Band Touch Scanner Start");
        System.out.println("=========================================");
        System.out.println();

        ApplicationConfig config =
                new ApplicationConfig();

        BinanceFuturesCandleMapper candleMapper =
                new BinanceFuturesCandleMapper();

        BitunixSymbolMapper symbolMapper =
                new BitunixSymbolMapper();

        HeikenAshiConverter haConverter =
                new HeikenAshiConverter();

        BollingerBandCalculator bbCalculator =
                new BollingerBandCalculator();

        TouchDetector touchDetector =
                new TouchDetector();

        MarketDataCache cache =
                new MarketDataCache();

        BinanceFuturesClient futuresClient =
                new BinanceFuturesClient(
                        config,
                        candleMapper);

        BinanceFuturesSymbolProvider futuresSymbolProvider =
                new BinanceFuturesSymbolProvider(
                        futuresClient);

        BitunixClient bitunixClient =
                new BitunixClient(config);

        BitunixSymbolProvider bitunixSymbolProvider =
                new BitunixSymbolProvider(
                        bitunixClient,
                        symbolMapper,
                        futuresSymbolProvider);

        MarketDataLoader marketDataLoader =
                new MarketDataLoader(
                        config,
                        futuresClient,
                        haConverter,
                        bbCalculator,
                        cache);

        MarketScanner marketScanner =
                new MarketScanner(
                        config,
                        touchDetector);

        ScannerApplication application =
                new ScannerApplication(
                        bitunixSymbolProvider,
                        marketDataLoader,
                        marketScanner,
                        config.getScannerDownloadThreads());

        List<ScanResult> results =
                application.run();

        Map<Timeframe, List<ScanResult>> grouped =
                new TreeMap<>();

        for (ScanResult result : results) {

            grouped.computeIfAbsent(
                    result.getTimeframe(),
                    key -> new java.util.ArrayList<>())
                    .add(result);

        }

        if (grouped.isEmpty()) {

            System.out.println("No symbols found.");

        } else {

            System.out.println("MULTI-TIMEFRAME");

            for (Map.Entry<Timeframe, List<ScanResult>> entry
                    : grouped.entrySet()) {

                System.out.println();
                System.out.println("-------------------------");
                System.out.println(entry.getKey());

                for (ScanResult result : entry.getValue()) {
                    System.out.println(result.getSymbol());
                }

            }

        }

        System.out.println();
        System.out.println("=========================================");
        System.out.println("  Bollinger Band Touch Scanner Complete");
        System.out.println("=========================================");

    }

}