package com.edison.scanner;

import java.math.BigDecimal;
import java.util.List;

import com.edison.scanner.common.Timeframe;
import com.edison.scanner.config.ApplicationConfig;
import com.edison.scanner.converter.HeikenAshiConverter;
import com.edison.scanner.detector.TouchDetector;
import com.edison.scanner.exchange.BinanceClient;
import com.edison.scanner.indicator.BollingerBandCalculator;
import com.edison.scanner.mapper.BinanceCandleMapper;
import com.edison.scanner.model.TouchResult;
import com.edison.scanner.model.market.Candle;
import com.edison.scanner.model.market.HeikenAshiCandle;
import com.edison.scanner.scanner.ConfluenceScanner;

/**
 * Application entry point.
 */
public final class Main {

    /**
     * Prevent instantiation.
     */
    private Main() {
    }

    /**
     * Starts the application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {

        System.out.println("=========================================");
        System.out.println("     Trading Confluence Scanner");
        System.out.println("=========================================");
        System.out.println();

        ApplicationConfig config = new ApplicationConfig();

        BinanceClient client = new BinanceClient(
                config,
                new BinanceCandleMapper());

        List<Candle> candles = client.getCandles(
                "BTCUSDT",
                Timeframe.D1,
                200);

        // Convert to Heiken Ashi
        HeikenAshiConverter converter = new HeikenAshiConverter();

        List<HeikenAshiCandle> haCandles =
                converter.convert(candles);

        // Create scanner
        ConfluenceScanner scanner =
                new ConfluenceScanner(
                        new BollingerBandCalculator(),
                        new TouchDetector());

        // Execute scan
        List<TouchResult> results =
                scanner.scan(
                        haCandles,
                        50,
                        BigDecimal.valueOf(0.2));

        results.forEach(System.out::println);
        System.out.println("end");

    }

    /**
     * Prints a candle.
     *
     * @param candle candle to print
     */
    private static void printCandle(Candle candle) {

        System.out.println("-----------------------------------------");
        System.out.println("Symbol     : " + candle.getSymbol());
        System.out.println("Timeframe  : " + candle.getTimeframe());
        System.out.println("Open Time  : " + candle.getOpenTime());
        System.out.println("Close Time : " + candle.getCloseTime());
        System.out.println("Open       : " + candle.getOpen());
        System.out.println("High       : " + candle.getHigh());
        System.out.println("Low        : " + candle.getLow());
        System.out.println("Close      : " + candle.getClose());
        System.out.println("Volume     : " + candle.getVolume());
        System.out.println("-----------------------------------------");

    }

}