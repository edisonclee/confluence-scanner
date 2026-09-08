package com.edison.scanner.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;

import com.edison.scanner.common.Timeframe;

/**
 * Application configuration loaded from application.properties.
 */
public final class ApplicationConfig {

	private final String binanceFuturesBaseUrl;
	private final int bbLength;
	private final BigDecimal bbMultiplier;
	private final int scannerCandleLimit;
	private final int scannerHaWarmup;
	private final List<Timeframe> scannerTimeframes;
	private final String bitunixBaseUrl;
	private final int scannerDownloadThreads;
	private final String bitunixUniverseFile;
	private final int bitunixUniverseMinimumAgeDays;
	private final int bitunixUniverseRefreshDays;

	public ApplicationConfig() {

		Properties properties = new Properties();

		try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {

			if (input == null) {
				throw new IllegalStateException("application.properties not found.");
			}

			properties.load(input);

		} catch (IOException ex) {

			throw new UncheckedIOException("Failed to load application.properties.", ex);
		}

		this.binanceFuturesBaseUrl = requireProperty(properties, "binance.futures.base-url");

		this.bbLength = Integer.parseInt(requireProperty(properties, "bb.length"));

		this.bbMultiplier = new BigDecimal(requireProperty(properties, "bb.multiplier"));

		this.scannerCandleLimit = Integer.parseInt(requireProperty(properties, "scanner.candle.limit"));

		this.scannerHaWarmup = Integer.parseInt(requireProperty(properties, "scanner.ha.warmup"));

		this.scannerTimeframes = parseTimeframes(requireProperty(properties, "scanner.timeframes"));

		this.bitunixBaseUrl = requireProperty(properties, "bitunix.base-url");

		this.scannerDownloadThreads = Integer.parseInt(requireProperty(properties, "scanner.download.threads"));

		this.bitunixUniverseFile = requireProperty(properties, "bitunix.universe.file");

		this.bitunixUniverseMinimumAgeDays = Integer
				.parseInt(requireProperty(properties, "bitunix.universe.minimum-age-days"));

		this.bitunixUniverseRefreshDays = Integer
				.parseInt(requireProperty(properties, "bitunix.universe.refresh-days"));
	}

	private static List<Timeframe> parseTimeframes(String value) {

		return List.of(value.split(",")).stream().map(String::trim).map(Timeframe::valueOf).toList();
	}

	private static String requireProperty(Properties properties, String key) {

		String value = properties.getProperty(key);

		if (value == null || value.isBlank()) {

			throw new IllegalStateException("Missing required property: " + key);
		}

		return value;
	}

	public String getBinanceFuturesBaseUrl() {
		return binanceFuturesBaseUrl;
	}

	public int getBbLength() {
		return bbLength;
	}

	public BigDecimal getBbMultiplier() {
		return bbMultiplier;
	}

	public int getScannerCandleLimit() {
		return scannerCandleLimit;
	}

	public int getScannerHaWarmup() {
		return scannerHaWarmup;
	}

	public List<Timeframe> getScannerTimeframes() {
		return scannerTimeframes;
	}

	public String getBitunixBaseUrl() {
		return bitunixBaseUrl;
	}

	public int getScannerDownloadThreads() {
		return scannerDownloadThreads;
	}

	public String getBitunixUniverseFile() {
		return bitunixUniverseFile;
	}

	public int getBitunixUniverseMinimumAgeDays() {
		return bitunixUniverseMinimumAgeDays;
	}

	public int getBitunixUniverseRefreshDays() {
		return bitunixUniverseRefreshDays;
	}
}