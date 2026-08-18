package com.edison.scanner.mapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.edison.scanner.common.Timeframe;
import com.edison.scanner.config.ApplicationConfig;
import com.edison.scanner.exceptions.ExchangeException;
import com.edison.scanner.model.market.Candle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Binance REST client.
 */
public final class BinanceFuturesClient {

    /**
     * Binance maximum candles per request.
     */
    private static final int MAX_BINANCE_LIMIT = 1000;

    private final ApplicationConfig config;

    private final HttpClient httpClient;

    private final BinanceFuturesCandleMapper candleMapper;
    
    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper();
    
    private Set<String> exchangeSymbols;

    /**
     * Creates a Binance client.
     *
     * @param config application configuration
     * @param candleMapper Binance candle mapper
     */
    public BinanceFuturesClient(
            ApplicationConfig config,
            BinanceFuturesCandleMapper candleMapper) {

        this.config = Objects.requireNonNull(config);
        this.candleMapper = Objects.requireNonNull(candleMapper);
        this.httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(
                                java.time.Duration.ofSeconds(15))
                        .build();

    }

    /**
     * Downloads historical candles.
     *
     * Supports requests larger than Binance's
     * 1000 candle limit by automatically paging.
     *
     * @param symbol trading symbol
     * @param timeframe timeframe
     * @param limit total candles requested
     *
     * @return candles ordered oldest -> newest
     */
    public List<Candle> getCandles(
            String symbol,
            Timeframe timeframe,
            int limit) {

        Objects.requireNonNull(symbol, "symbol");
        Objects.requireNonNull(timeframe, "timeframe");

        if (symbol.isBlank()) {
            throw new IllegalArgumentException(
                    "symbol must not be blank.");
        }

        if (limit <= 0) {
            throw new IllegalArgumentException(
                    "limit must be greater than zero.");
        }

        List<Candle> allCandles = new ArrayList<>();

        Long endTime = null;

        int remaining = limit;

        while (remaining > 0) {

            int requestLimit =
                    Math.min(MAX_BINANCE_LIMIT, remaining);

            List<Candle> batch =
                    downloadCandles(
                            symbol,
                            timeframe,
                            requestLimit,
                            endTime);

            if (batch.isEmpty()) {
                break;
            }

            /*
             * Prepend older candles.
             */
            allCandles.addAll(0, batch);

            remaining -= batch.size();

            /*
             * Next request ends before the oldest candle
             * already downloaded.
             */
            endTime =
                    batch.get(0)
                         .getOpenTime()
                         .toEpochMilli() - 1;

        }

        return List.copyOf(allCandles);

    }

    /**
     * Downloads one batch from Binance.
     */
    private List<Candle> downloadCandles(
            String symbol,
            Timeframe timeframe,
            int limit,
            Long endTime) {

        URI uri =
                buildKlineUri(
                        symbol,
                        timeframe.getInterval(),
                        limit,
                        endTime);

        HttpRequest request =
                HttpRequest.newBuilder(uri)
                        .timeout(
                                java.time.Duration.ofSeconds(30))
                        .header("User-Agent", "ConfluenceScanner/1.0")
                        .header("Accept", "application/json")
                        .GET()
                        .build();

        HttpResponse<String> response =
		        send(request);

		return candleMapper.map(
		        symbol,
		        timeframe,
		        response.body());

    }

    /**
     * Builds Binance Kline endpoint.
     */
    private URI buildKlineUri(
            String symbol,
            String interval,
            int limit,
            Long endTime) {

        StringBuilder url =
                new StringBuilder(
                        String.format(
                        		"%s/fapi/v1/klines?symbol=%s&interval=%s&limit=%d",
                                config.getBinanceFuturesBaseUrl(),
                                encode(symbol),
                                encode(interval),
                                limit));

        if (endTime != null) {
            url.append("&endTime=").append(endTime);
        }

        return URI.create(url.toString());

    }

    /**
     * Validates Binance response.
     */
    private void validateResponse (
            HttpResponse<String> response) {

        if (response.statusCode() != 200) {

            throw new ExchangeException(
                    "Unexpected response status: "
                            + response.statusCode());

        }

    }
    
    /**
     * Sends an HTTP request to Binance Futures.
     *
     * @param request HTTP request
     *
     * @return HTTP response
     */
    private HttpResponse<String> send(
            HttpRequest request) {

        try {

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString());

            validateResponse(response);

            return response;

        } catch (InterruptedException ex) {
        	ex.printStackTrace();

            Thread.currentThread().interrupt();

            throw new ExchangeException(
                    "Failed to communicate with Binance Futures.",
                    ex);

        } catch (IOException ex) {
        	ex.printStackTrace();

            throw new ExchangeException(
                    "Failed to communicate with Binance Futures.",
                    ex);

        }

    }

    /**
     * URL encodes a value.
     */
    private String encode(
            String value) {

        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8);

    }
    
    public Set<String> getExchangeSymbols() {

        if (exchangeSymbols != null) {
            return exchangeSymbols;
        }

        HttpRequest request =
                HttpRequest.newBuilder(
                        buildExchangeInfoUri())
                        .timeout(
                                java.time.Duration.ofSeconds(30))
                        .header("User-Agent", "ConfluenceScanner/1.0")
                        .header("Accept", "application/json")
                        .GET()
                        .build();

        HttpResponse<String> response =
                send(request);

        Set<String> symbols =
                new HashSet<>();

        try {

            JsonNode root =
                    OBJECT_MAPPER.readTree(
                            response.body());

            for (JsonNode symbol : root.get("symbols")) {

                if (!"TRADING".equals(
                        symbol.get("status").asText())) {
                    continue;
                }

                symbols.add(
                        symbol.get("symbol").asText());

            }

        } catch (IOException ex) {
        	ex.printStackTrace();

            throw new UncheckedIOException(ex);

        }

        exchangeSymbols = Set.copyOf(symbols);

        return exchangeSymbols;

    }
    
    private URI buildExchangeInfoUri() {

        return URI.create(
                config.getBinanceFuturesBaseUrl()
                        + "/fapi/v1/exchangeInfo");

    }

	public void setExchangeSymbols(Set<String> exchangeSymbols) {
		this.exchangeSymbols = exchangeSymbols;
	}

}