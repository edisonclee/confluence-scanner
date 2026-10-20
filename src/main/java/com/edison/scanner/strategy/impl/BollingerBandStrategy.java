
package com.edison.scanner.strategy.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.edison.scanner.common.Timeframe;
import com.edison.scanner.config.ApplicationConfig;
import com.edison.scanner.detector.TouchDetector;
import com.edison.scanner.market.MarketData;
import com.edison.scanner.market.TimeframeData;
import com.edison.scanner.model.ScanResult;
import com.edison.scanner.model.ScannerRequest;
import com.edison.scanner.model.indicator.BollingerBand;
import com.edison.scanner.model.market.HeikenAshiCandle;
import com.edison.scanner.strategy.ScannerStrategy;

@Component
public final class BollingerBandStrategy implements ScannerStrategy {

    private static final Logger log =
            LoggerFactory.getLogger(BollingerBandStrategy.class);

    private static final Set<String> DIAGNOSTIC_SYMBOLS =
            Set.of("BTCUSDT", "ETHUSDT", "XRPUSDT");

    private final ApplicationConfig config;
    private final TouchDetector touchDetector;

    public BollingerBandStrategy(
            ApplicationConfig config,
            TouchDetector touchDetector) {

        this.config = Objects.requireNonNull(config);
        this.touchDetector = Objects.requireNonNull(touchDetector);
    }

    @Override
    public List<ScanResult> scan(
            MarketData marketData,
            ScannerRequest request) {

        Objects.requireNonNull(marketData, "marketData");
        Objects.requireNonNull(request, "request");

        List<ScanResult> results = new ArrayList<>();

        String symbol =
                marketData.getSymbol().getExchangeSymbol();

        boolean diagnostic =
                DIAGNOSTIC_SYMBOLS.contains(symbol);

        // H1 is always the chart timeframe.
        TimeframeData chart = marketData.getChart(Timeframe.H1);

        if (chart == null) {
            return List.of();
        }

        List<HeikenAshiCandle> candles =
                chart.getHeikenAshiCandles();

        if (candles.isEmpty()) {
            return List.of();
        }

        HeikenAshiCandle latestChart =
                candles.get(candles.size() - 1);

        // Check only the BB timeframes selected by the user.
        for (Timeframe bbTimeframe : request.getBbTimeframes()) {

            TimeframeData bbData =
                    marketData.getChart(bbTimeframe);

            if (bbData == null) {
                continue;
            }

            BollingerBand band =
                    bbData.getLatestBollingerBand();

            if (band == null) {
                continue;
            }

            boolean upperTouched =
                    touches(latestChart, band.getUpperBand());

            boolean basisTouched =
                    touches(latestChart, band.getBasisBand());

            boolean lowerTouched =
                    touches(latestChart, band.getLowerBand());

            boolean touched =
                    touchDetector.isTouched(latestChart, band);

            // One concise diagnostic line per target symbol
            // and selected BB timeframe.
            if (diagnostic) {
                log.info(
                    "[BB-DIAG] symbol={} chartTF=H1 bbTF={} "
                    + "HA[O={},H={},L={},C={}] "
                    + "BB[U={},B={},L={}] "
                    + "touch[U={},B={},L={}] result={}",
                    symbol,
                    bbTimeframe,
                    latestChart.getOpen(),
                    latestChart.getHigh(),
                    latestChart.getLow(),
                    latestChart.getClose(),
                    band.getUpperBand(),
                    band.getBasisBand(),
                    band.getLowerBand(),
                    upperTouched,
                    basisTouched,
                    lowerTouched,
                    touched
                );
            }

            if (!touched) {
                continue;
            }

            BigDecimal bbWidthPercent =
                    band.getUpperBand()
                        .subtract(band.getLowerBand())
                        .divide(
                            band.getBasisBand(),
                            8,
                            RoundingMode.HALF_UP
                        )
                        .multiply(BigDecimal.valueOf(100));

            results.add(
                new ScanResult(
                    symbol,
                    bbTimeframe,
                    bbWidthPercent
                )
            );
        }

        return List.copyOf(results);
    }

    /**
     * A wick touches a band when the candle's high-low range
     * includes the band's value.
     */
    private boolean touches(
            HeikenAshiCandle candle,
            BigDecimal bandValue) {

        return candle.getLow().compareTo(bandValue) <= 0
                && candle.getHigh().compareTo(bandValue) >= 0;
    }

    @Override
    public String getName() {
        return "bb";
    }

    public ApplicationConfig getConfig() {
        return config;
    }
}
