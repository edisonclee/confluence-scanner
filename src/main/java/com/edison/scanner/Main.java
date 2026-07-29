package com.edison.scanner;

import com.edison.scanner.common.Timeframe;
import com.edison.scanner.config.ApplicationConfig;
import com.edison.scanner.exchange.BinanceClient;
import com.edison.scanner.model.market.Candle;

import java.util.List;

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

        BinanceClient client =
                new BinanceClient(
                        config.get("binance.base-url"));

        System.out.println("Downloading BTCUSDT weekly candles...");
        System.out.println();

        List<Candle> candles =
                client.getCandles(
                        "BTCUSDT",
                        Timeframe.W1,
                        5);

        System.out.println("Retrieved "
                + candles.size()
                + " candles.");

        System.out.println();

        Candle candle = candles.getFirst();

        printCandle(candle);

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