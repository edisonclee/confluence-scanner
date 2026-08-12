package com.edison.scanner.strategy.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.edison.scanner.common.Timeframe;
import com.edison.scanner.config.ApplicationConfig;
import com.edison.scanner.detector.TouchDetector;
import com.edison.scanner.market.MarketData;
import com.edison.scanner.market.TimeframeData;
import com.edison.scanner.model.ScanResult;
import com.edison.scanner.model.indicator.BollingerBand;
import com.edison.scanner.model.market.HeikenAshiCandle;
import com.edison.scanner.strategy.ScannerStrategy;

/**
 * Scans prepared market data for Bollinger Band touches.
 */
@Component
public final class BollingerBandStrategy implements ScannerStrategy {

    private final ApplicationConfig config;

    private final TouchDetector touchDetector;

    public BollingerBandStrategy(
            ApplicationConfig config,
            TouchDetector touchDetector) {

        this.config = Objects.requireNonNull(config);
        this.touchDetector =
                Objects.requireNonNull(touchDetector);

    }

    /**
     * Scans every configured timeframe.
     *
     * @param marketData prepared market data
     *
     * @return scan results
     */
    public List<ScanResult> scan(
            MarketData marketData) {

        Objects.requireNonNull(
                marketData,
                "marketData");

        List<ScanResult> results =
                new ArrayList<>();

        for (Timeframe chartTimeframe :
                config.getScannerTimeframes()) {

            TimeframeData chart =
                    marketData.getChart(
                            chartTimeframe);

            TimeframeData higher =
                    marketData.getHigher(
                            chartTimeframe);

            if (chart == null ||
                    higher == null) {
                continue;
            }

            HeikenAshiCandle latestChart =
                    chart.getLatestHeikenAshi();

            BollingerBand latestBand =
                    higher.getLatestBollingerBand();

            if (latestChart == null ||
                    latestBand == null) {
                continue;
            }

            if (!touchDetector.isTouched(
                    latestChart,
                    latestBand)) {
                continue;
            }

            BigDecimal bbWidthPercent =
                    latestBand.getUpperBand()
                            .subtract(
                                    latestBand.getLowerBand())
                            .divide(
                                    latestBand.getBasisBand(),
                                    8,
                                    RoundingMode.HALF_UP)
                            .multiply(
                                    BigDecimal.valueOf(100));

            results.add(
                    new ScanResult(
                            marketData.getSymbol()
                                    .getExchangeSymbol(),
                            chartTimeframe,
                            bbWidthPercent));

        }

        return List.copyOf(results);

    }

    @Override
    public String getName() {
        return "bb";
    }

}