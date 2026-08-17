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

    /**
     * Binance REST API base URL.
     */
    private final String binanceFuturesBaseUrl;

    /**
     * Bollinger Band length.
     */
    private final int bbLength;

    /**
     * Bollinger Band multiplier.
     */
    private final BigDecimal bbMultiplier;

    /**
     * Number of candles to download.
     */
    private final int scannerCandleLimit;
    
    private final int scannerHaWarmup;
    
    private final List<Timeframe> scannerTimeframes;
    
    private final String bitunixBaseUrl;
    
    private final int scannerDownloadThreads;

    /**
     * Creates the application configuration.
     */
    public ApplicationConfig() {

        Properties properties = new Properties();

        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                throw new IllegalStateException(
                        "application.properties not found.");
            }

            properties.load(input);

        } catch (IOException ex) {
        	ex.printStackTrace();
            throw new UncheckedIOException(
                    "Failed to load application.properties.",
                    ex);
        }

        this.binanceFuturesBaseUrl = requireProperty(
                properties,
                "binance.futures.base-url");

        this.bbLength = Integer.parseInt(
                requireProperty(
                        properties,
                        "bb.length"));

        this.bbMultiplier = new BigDecimal(
                requireProperty(
                        properties,
                        "bb.multiplier"));

        this.scannerCandleLimit = Integer.parseInt(
                requireProperty(
                        properties,
                        "scanner.candle.limit"));
		this.scannerHaWarmup = Integer.parseInt(
                requireProperty(
                        properties,
                        "scanner.ha.warmup"));
        scannerTimeframes = parseTimeframes(
                requireProperty(
                        properties,
                        "scanner.timeframes"));
        
        this.bitunixBaseUrl =
                requireProperty(
                        properties,
                        "bitunix.base-url");
        
        this.scannerDownloadThreads =
                Integer.parseInt(
                        requireProperty(
                                properties,
                                "scanner.download.threads"));

    }
    
    private static List<Timeframe> parseTimeframes(
            String value) {

        return List.of(value.split(","))
                .stream()
                .map(String::trim)
                .map(Timeframe::valueOf)
                .toList();

    }

    /**
     * Returns the Binance REST API base URL.
     */
    public String getBinanceFuturesBaseUrl() {
        return binanceFuturesBaseUrl;
    }

    /**
     * Returns the Bollinger Band length.
     */
    public int getBbLength() {
        return bbLength;
    }

    /**
     * Returns the Bollinger Band multiplier.
     */
    public BigDecimal getBbMultiplier() {
        return bbMultiplier;
    }

    /**
     * Returns the candle download limit.
     */
    public int getScannerCandleLimit() {
        return scannerCandleLimit;
    }

    /**
     * Returns a required property.
     *
     * @param properties loaded properties
     * @param key property key
     * @return property value
     */
    private static String requireProperty(
            Properties properties,
            String key) {

        String value = properties.getProperty(key);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing required property: " + key);
        }

        return value;

    }

	public List<Timeframe> getScannerTimeframes() {
		return scannerTimeframes;
	}

	public int getScannerHaWarmup() {
		return scannerHaWarmup;
	}
	
	public String getBitunixBaseUrl() {
	    return bitunixBaseUrl;
	}

	public int getScannerDownloadThreads() {
		return scannerDownloadThreads;
	}

}