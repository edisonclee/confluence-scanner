package com.edison.scanner.market;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.edison.scanner.common.Timeframe;
import com.edison.scanner.config.ApplicationConfig;
import com.edison.scanner.converter.HeikenAshiConverter;
import com.edison.scanner.exchange.BinanceFuturesClient;
import com.edison.scanner.indicator.BollingerBandCalculator;
import com.edison.scanner.model.indicator.BollingerBand;
import com.edison.scanner.model.market.Candle;
import com.edison.scanner.model.market.HeikenAshiCandle;
import com.edison.scanner.model.market.TradingSymbol;

/**
 * Loads all market data for one trading symbol.
 */
public final class MarketDataLoader {

    private final ApplicationConfig config;

    private final BinanceFuturesClient client;

    private final HeikenAshiConverter heikenAshiConverter;

    private final BollingerBandCalculator bollingerBandCalculator;
    
    private final MarketDataCache cache;

    public MarketDataLoader(
            ApplicationConfig config,
            BinanceFuturesClient client,
            HeikenAshiConverter heikenAshiConverter,
            BollingerBandCalculator bollingerBandCalculator,
            MarketDataCache cache) {

        this.config = Objects.requireNonNull(config);
        this.client = Objects.requireNonNull(client);
        this.heikenAshiConverter =
                Objects.requireNonNull(heikenAshiConverter);
        this.bollingerBandCalculator =
                Objects.requireNonNull(bollingerBandCalculator);
        this.cache =
                Objects.requireNonNull(cache);

    }

    /**
     * Returns cached market data.
     *
     * @param symbol trading symbol
     *
     * @return cached market data
     */
    public MarketData get(
            TradingSymbol symbol) {

        return cache.get(symbol);

    }
    
    /**
     * Downloads and caches market data.
     *
     * @param symbol trading symbol
     */
    public void preload(
            TradingSymbol symbol) {

        if (cache.contains(symbol)) {
            return;
        }

        MarketData marketData =
                new MarketData(symbol);

        Set<Timeframe> requiredTimeframes =
                new LinkedHashSet<>();

        for (Timeframe timeframe :
                config.getScannerTimeframes()) {

            requiredTimeframes.add(timeframe);

            Timeframe higher =
                    timeframe.getHigherTimeframe();

            if (higher != null) {
                requiredTimeframes.add(higher);
            }

        }

        for (Timeframe timeframe : requiredTimeframes) {

            List<Candle> candles =
                    client.getCandles(
                            symbol.getExchangeSymbol(),
                            timeframe,
                            config.getScannerCandleLimit());

            List<HeikenAshiCandle> ha =
                    heikenAshiConverter.convert(candles);

            if (ha.size() > config.getScannerHaWarmup()) {

                ha = ha.subList(
                        config.getScannerHaWarmup(),
                        ha.size());

            }

            List<BollingerBand> bands =
                    bollingerBandCalculator.calculate(
                            ha,
                            config.getBbLength(),
                            config.getBbMultiplier());

            marketData.put(
                    timeframe,
                    new TimeframeData(
                            candles,
                            ha,
                            bands));

        }

        cache.put(marketData);

    }

}