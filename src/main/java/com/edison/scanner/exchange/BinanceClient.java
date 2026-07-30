package com.edison.scanner.exchange;

import com.edison.scanner.common.Timeframe;
import com.edison.scanner.config.ApplicationConfig;
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
import java.util.Objects;

/**
 * Binance REST client.
 */
public final class BinanceClient {

    private final ApplicationConfig config;

    private final HttpClient httpClient;

    private final BinanceCandleMapper candleMapper;

    /**
     * Creates a Binance client.
     *
     * @param config application configuration
     */
    public BinanceClient(
            ApplicationConfig config,
            BinanceCandleMapper candleMapper) {

        this.config = Objects.requireNonNull(config);
        this.candleMapper = Objects.requireNonNull(candleMapper);
        this.httpClient = HttpClient.newHttpClient();

    }

    /**
     * Downloads historical candles.
     *
     * @param symbol trading symbol
     * @param timeframe timeframe
     * @param limit number of candles
     * @return candles
     */
    public List<Candle> getCandles(
            String symbol,
            Timeframe timeframe,
            int limit) {
    	Objects.requireNonNull(symbol, "symbol");
    	Objects.requireNonNull(timeframe, "timeframe");

    	if (symbol.isBlank()) {
    	    throw new IllegalArgumentException("symbol must not be blank.");
    	}

    	if (limit <= 0) {
    	    throw new IllegalArgumentException("limit must be greater than zero.");
    	}

        URI uri = buildKlineUri(symbol, timeframe.getBinanceInterval(), limit);

        HttpRequest request = HttpRequest.newBuilder(uri)
                .GET()
                .build();

        try {

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            validateResponse(response);

            return candleMapper.map(
                    symbol,
                    timeframe,
                    response.body());

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BinanceException(
                    "Failed to retrieve Binance candles.",
                    ex);
        } catch (IOException ex) {
            throw new BinanceException(
                    "Failed to retrieve Binance candles.",
                    ex);
        }

    }

    private URI buildKlineUri(
            String symbol,
            String interval,
            int limit) {

        String url = String.format(
                "%s/api/v3/klines?symbol=%s&interval=%s&limit=%d",
                config.getBaseUrl(),
                encode(symbol),
                encode(interval),
                limit);

        return URI.create(url);

    }

    private void validateResponse(HttpResponse<?> response) {

        if (response.statusCode() != 200) {

            throw new BinanceException(
                    "Unexpected response status: " + response.statusCode());

        }

    }

    private String encode(String value) {

        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8);

    }

}