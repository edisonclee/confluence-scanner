package com.edison.scanner;

import java.util.ArrayList;
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

        ScannerEngine application =
                new ScannerEngine(
                        bitunixSymbolProvider,
                        marketDataLoader,
                        marketScanner,
                        config.getScannerDownloadThreads());

        List<ScanResult> results =
                application.run();

        if (results.isEmpty()) {

            System.out.println("No symbols found.");

        } else {

            /*
             * Group by symbol.
             */
            Map<String, List<ScanResult>> bySymbol =
                    new TreeMap<>();

            for (ScanResult result : results) {

                bySymbol
                        .computeIfAbsent(
                                result.getSymbol(),
                                key -> new ArrayList<>())
                        .add(result);

            }

            /*
             * Multi-timeframe section.
             */
            System.out.println("MULTI-TIMEFRAME");
            System.out.println();

            List<ScanResult> singleTimeframeResults =
                    new ArrayList<>();

            List<Map.Entry<String, List<ScanResult>>> multiTimeframeResults =
                    new ArrayList<>();

            for (Map.Entry<String, List<ScanResult>> entry
                    : bySymbol.entrySet()) {

                if (entry.getValue().size() > 1) {

                    multiTimeframeResults.add(entry);

                } else {

                    singleTimeframeResults.add(
                            entry.getValue().get(0));

                }

            }

            /*
             * Sort multi-timeframe by largest BB Width.
             */
            multiTimeframeResults.sort(

                    (left, right) -> {

                        var leftWidth =
                                left.getValue()
                                        .stream()
                                        .map(ScanResult::getBbWidthPercent)
                                        .max(java.math.BigDecimal::compareTo)
                                        .orElse(java.math.BigDecimal.ZERO);

                        var rightWidth =
                                right.getValue()
                                        .stream()
                                        .map(ScanResult::getBbWidthPercent)
                                        .max(java.math.BigDecimal::compareTo)
                                        .orElse(java.math.BigDecimal.ZERO);

                        return rightWidth.compareTo(leftWidth);

                    });

            for (Map.Entry<String, List<ScanResult>> entry
                    : multiTimeframeResults) {

                String timeframes =
                        entry.getValue()
                                .stream()
                                .map(r -> r.getTimeframe().toString())
                                .sorted()
                                .reduce((a, b) -> a + ", " + b)
                                .orElse("");

                var width =
                        entry.getValue()
                                .stream()
                                .map(ScanResult::getBbWidthPercent)
                                .max(java.math.BigDecimal::compareTo)
                                .orElse(java.math.BigDecimal.ZERO);

                System.out.printf(
                        "%-18s [%-12s] %7.2f%%%n",
                        entry.getKey(),
                        timeframes,
                        width);

            }

            /*
             * Group single-timeframe results.
             */
            Map<Timeframe, List<ScanResult>> grouped =
                    new TreeMap<>();

            for (ScanResult result : singleTimeframeResults) {

                grouped
                        .computeIfAbsent(
                                result.getTimeframe(),
                                key -> new ArrayList<>())
                        .add(result);

            }

            /*
             * Sort each timeframe by BB Width descending.
             */
            for (List<ScanResult> timeframeResults
                    : grouped.values()) {

                timeframeResults.sort(

                        (left, right) ->

                                right.getBbWidthPercent()
                                        .compareTo(
                                                left.getBbWidthPercent()));

            }

            /*
             * Print.
             */
            for (Map.Entry<Timeframe, List<ScanResult>> entry
                    : grouped.entrySet()) {

                System.out.println();
                System.out.println("-------------------------");
                System.out.printf(
                        "%s Timeframe | %s BB Timeframe%n",
                        entry.getKey(),
                        entry.getKey().getHigherTimeframe());

                for (ScanResult result
                        : entry.getValue()) {

                    System.out.printf(
                            "%-18s %7.2f%%%n",
                            result.getSymbol(),
                            result.getBbWidthPercent());

                }

            }

        }

        System.out.println();
        System.out.println("=========================================");
        System.out.println("  Bollinger Band Touch Scanner Complete");
        System.out.println("=========================================");

    }

}