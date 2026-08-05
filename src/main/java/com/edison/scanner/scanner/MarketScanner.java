package com.edison.scanner.scanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.edison.scanner.common.Timeframe;
import com.edison.scanner.config.ApplicationConfig;
import com.edison.scanner.detector.TouchDetector;
import com.edison.scanner.market.MarketData;
import com.edison.scanner.market.TimeframeData;
import com.edison.scanner.model.ScanResult;
import com.edison.scanner.model.indicator.BollingerBand;
import com.edison.scanner.model.market.HeikenAshiCandle;

/**
 * Scans prepared market data for Bollinger Band touches.
 */
public final class MarketScanner {

    private final ApplicationConfig config;

    private final TouchDetector touchDetector;

    public MarketScanner(
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

            results.add(
                    new ScanResult(
                            marketData.getSymbol()
                                    .getExchangeSymbol(),
                            chartTimeframe));

        }

        return List.copyOf(results);

    }

}