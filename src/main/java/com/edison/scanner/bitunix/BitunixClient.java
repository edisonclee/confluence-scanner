package com.edison.scanner.bitunix;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

import com.edison.scanner.config.ApplicationConfig;
import com.edison.scanner.exceptions.ExchangeException;

/**
 * Bitunix REST client.
 */
public final class BitunixClient {

    /**
     * Application configuration.
     */
    private final ApplicationConfig config;

    /**
     * HTTP client.
     */
    private final HttpClient httpClient;

    /**
     * Creates a Bitunix client.
     *
     * @param config application configuration
     */
    public BitunixClient(
            ApplicationConfig config) {

        this.config = Objects.requireNonNull(
                config,
                "config");

        this.httpClient = HttpClient.newHttpClient();

    }

    /**
     * Downloads all futures trading pairs.
     *
     * @return raw JSON response.
     */
    public String getTradingPairs() {

        URI uri = URI.create(
                config.getBitunixBaseUrl()
                        + "/api/v1/futures/market/trading_pairs");

        HttpRequest request =
                HttpRequest.newBuilder(uri)
                        .GET()
                        .build();

        try {

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString());

            validateResponse(response);

            return response.body();

        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();

            throw new ExchangeException(
                    "Failed to retrieve Bitunix trading pairs.",
                    ex);

        } catch (IOException ex) {

            throw new ExchangeException(
                    "Failed to retrieve Bitunix trading pairs.",
                    ex);

        }

    }

    /**
     * Validates the HTTP response.
     *
     * @param response HTTP response
     */
    private void validateResponse(
            HttpResponse<?> response) {

        if (response.statusCode() != 200) {

            throw new ExchangeException(
                    "Unexpected Bitunix response status: "
                            + response.statusCode());

        }

    }

}