package com.edison.scanner.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents all supported chart timeframes used by the scanner.
 *
 * <p>
 * Each timeframe defines:
 * <ul>
 *     <li>The display name shown in logs and reports.</li>
 *     <li>The Binance interval used when requesting candles.</li>
 *     <li>The default higher timeframe used by the dynamic
 *     Bollinger Band strategy.</li>
 * </ul>
 *
 * <p>
 * Default mapping:
 *
 * <pre>
 * 5m  → 15m
 * 15m → 1h
 * 1h  → 4h
 * 4h  → 1d
 * 1d  → 1w
 * </pre>
 */
public enum Timeframe {

    M5("5m", "5m"),
    M15("15m", "15m"),
    H1("1h", "1h"),
    H4("4h", "4h"),
    D1("1d", "1d"),
    W1("1w", "1w");

    /**
     * Lookup table for Binance intervals.
     */
    private static final Map<String, Timeframe> LOOKUP = new HashMap<>();

    static {

        for (Timeframe timeframe : values()) {
            LOOKUP.put(
                    timeframe.binanceInterval.toLowerCase(),
                    timeframe);
        }

        M5.higherTimeframe = M15;
        M15.higherTimeframe = H1;
        H1.higherTimeframe = H4;
        H4.higherTimeframe = D1;
        D1.higherTimeframe = W1;
        W1.higherTimeframe = null;

    }

    /**
     * Display name.
     */
    private final String displayName;

    /**
     * Binance interval.
     */
    private final String binanceInterval;

    /**
     * Default higher timeframe.
     */
    private Timeframe higherTimeframe;

    /**
     * Creates a timeframe.
     *
     * @param displayName
     *         Display name.
     * @param binanceInterval
     *         Binance interval.
     */
    Timeframe(
            String displayName,
            String binanceInterval) {

        this.displayName = displayName;
        this.binanceInterval = binanceInterval;

    }

    /**
     * Returns the display name.
     *
     * @return display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the Binance interval.
     *
     * @return Binance interval.
     */
    public String getBinanceInterval() {
        return binanceInterval;
    }

    /**
     * Returns the default higher timeframe.
     *
     * @return higher timeframe or {@code null} if this is the highest supported timeframe.
     */
    public Timeframe getHigherTimeframe() {
        return higherTimeframe;
    }

    /**
     * Returns the timeframe associated with the given Binance interval.
     *
     * @param interval
     *         Binance interval.
     *
     * @return matching timeframe.
     *
     * @throws IllegalArgumentException
     *         if the interval is unsupported.
     */
    public static Timeframe fromInterval(String interval) {

        Timeframe timeframe = LOOKUP.get(interval.toLowerCase());

        if (timeframe == null) {
            throw new IllegalArgumentException(
                    "Unsupported timeframe: " + interval);
        }

        return timeframe;

    }

    @Override
    public String toString() {
        return displayName;
    }

}