package com.edison.scanner.common;

import java.time.Duration;
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

	M5("5m", "5m", java.time.Duration.ofMinutes(5)),
	M15("15m", "15m", java.time.Duration.ofMinutes(15)),
	H1("1h", "1h", java.time.Duration.ofHours(1)),
	H4("4h", "4h", java.time.Duration.ofHours(4)),
	D1("1d", "1d", java.time.Duration.ofDays(1)),
	W1("1w", "1w", java.time.Duration.ofDays(7)),
	MN1("1M", "1M", java.time.Duration.ofDays(30));

    /**
     * Lookup table for Binance intervals.
     */
    private static final Map<String, Timeframe> LOOKUP = new HashMap<>();

    static {

        for (Timeframe timeframe : values()) {
        	LOOKUP.put(
        	        timeframe.interval.toLowerCase(),
        	        timeframe);
        }

        M5.higherTimeframe = M15;
        M15.higherTimeframe = H1;
        H1.higherTimeframe = H4;
        H4.higherTimeframe = D1;
        D1.higherTimeframe = W1;
        W1.higherTimeframe = MN1;
        MN1.higherTimeframe = null;

    }

    /**
     * Display name.
     */
    private final String displayName;

    private final String interval;

    private final Duration duration;

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
            String interval,
            Duration duration) {

        this.displayName = displayName;
        this.interval = interval;
        this.duration = duration;

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
    public String getInterval() {
        return interval;
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
        if (interval == null) {
            throw new IllegalArgumentException("interval must not be null.");
        }

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

	public Duration getDuration() {
		return duration;
	}

}