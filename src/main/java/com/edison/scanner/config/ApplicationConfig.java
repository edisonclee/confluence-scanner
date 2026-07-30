package com.edison.scanner.config;

import java.util.Objects;

/**
 * Application configuration.
 */
public final class ApplicationConfig {

    /**
     * Binance REST API base URL.
     */
    private final String baseUrl;

    /**
     * Creates the default application configuration.
     */
    public ApplicationConfig() {
        this("https://api.binance.com");
    }

    /**
     * Creates an application configuration.
     *
     * @param baseUrl Binance REST API base URL
     */
    public ApplicationConfig(String baseUrl) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
    }

    /**
     * Returns the Binance REST API base URL.
     *
     * @return base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

}