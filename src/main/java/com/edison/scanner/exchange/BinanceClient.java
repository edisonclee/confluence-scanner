package com.edison.scanner.exchange;

import com.edison.scanner.common.Timeframe;
import com.edison.scanner.exception.BinanceException;
import com.edison.scanner.mapper.BinanceCandleMapper;
import com.edison.scanner.model.market.Candle;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Client responsible for downloading historical candle data from Binance.
 *
 * <p>
 * This class communicates only with the Binance REST API.
 * It does not perform any indicator calculations or business logic.
 * </p>
 */
public class BinanceClient {

    /**
     * Base Binance REST URL.
     */
    private final String baseUrl;

    /**
     * HTTP client.
     */
    private final HttpClient httpClient;

    /**
     * Maps Binance JSON responses into domain objects.
     */
    private final BinanceCandleMapper candleMapper;

    /**
     * Creates a Binance client.
     *
     * @param baseUrl Binance REST base URL.
     */
    public BinanceClient(String baseUrl) {

        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.candleMapper = new BinanceCandleMapper();

    }

    /**
     * Downloads historical candles.
     *
     * @param symbol trading symbol
     * @param timeframe candle timeframe
     * @param limit number of candles
     *
     * @return immutable list of candles
     *
     * @throws BinanceException if communication fails
     */
    public List<Candle> getCandles(
            String symbol,
            Timeframe timeframe,
            int limit) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildKlineUri(symbol, timeframe, limit))
                .GET()
                .build();

        try {

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString());

            validateResponse(response);

            return candleMapper.map(
                    symbol,
                    timeframe,
                    response.body());

        } catch (IOException | InterruptedException ex) {

            Thread.currentThread().interrupt();

            throw new BinanceException(
                    "Unable to retrieve Binance candles.",
                    ex);

        }

    }

    /**
     * Builds the Binance Klines endpoint URI.
     *
     * @param symbol trading symbol
     * @param timeframe timeframe
     * @param limit candle limit
     *
     * @return request URI
     */
    private URI buildKlineUri(
            String symbol,
            Timeframe timeframe,
            int limit) {

        String uri = String.format(
                "%s/api/v3/klines?symbol=%s&interval=%s&limit=%d",
                baseUrl,
                encode(symbol),
                encode(timeframe.getBinanceInterval()),
                limit);

        return URI.create(uri);

    }

    /**
     * Validates the HTTP response.
     *
     * @param response HTTP response
     */
    private void validateResponse(HttpResponse<String> response) {

        if (response.statusCode() != 200) {

            throw new BinanceException(
                    "Unexpected HTTP status: "
                            + response.statusCode()
                            + System.lineSeparator()
                            + response.body());

        }

    }

    /**
     * URL-encodes a value.
     *
     * @param value value to encode
     *
     * @return encoded value
     */
    private String encode(String value) {

        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8);

    }

}