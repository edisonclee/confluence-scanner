package com.edison.scanner.common;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents all supported chart timeframes used by the scanner.
 */
public enum Timeframe {

	M5("5m", "5m", Duration.ofMinutes(5)), M15("15m", "15m", Duration.ofMinutes(15)),
	H1("1h", "1h", Duration.ofHours(1)), H4("4h", "4h", Duration.ofHours(4)), D1("1d", "1d", Duration.ofDays(1)),
	W1("1w", "1w", Duration.ofDays(7)), MN1("1M", "1M", Duration.ofDays(30));

	/**
	 * Lookup table by exchange interval.
	 */
	private static final Map<String, Timeframe> LOOKUP = new HashMap<>();

	static {

		for (Timeframe timeframe : values()) {
			LOOKUP.put(timeframe.interval.toLowerCase(), timeframe);
		}

	}

	private final String displayName;

	private final String interval;

	private final Duration duration;

	Timeframe(String displayName, String interval, Duration duration) {

		this.displayName = displayName;
		this.interval = interval;
		this.duration = duration;

	}

	public String getDisplayName() {
		return displayName;
	}

	public String getInterval() {
		return interval;
	}

	public Duration getDuration() {
		return duration;
	}

	public static Timeframe fromInterval(String interval) {

		if (interval == null) {
			throw new IllegalArgumentException("interval must not be null.");
		}

		Timeframe timeframe = LOOKUP.get(interval.toLowerCase());

		if (timeframe == null) {
			throw new IllegalArgumentException("Unsupported timeframe: " + interval);
		}

		return timeframe;

	}

	@Override
	public String toString() {
		return displayName;
	}

}