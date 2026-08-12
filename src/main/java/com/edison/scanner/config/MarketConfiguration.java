package com.edison.scanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.edison.scanner.converter.HeikenAshiConverter;
import com.edison.scanner.exchange.BinanceFuturesClient;
import com.edison.scanner.indicator.BollingerBandCalculator;
import com.edison.scanner.market.MarketDataCache;
import com.edison.scanner.market.MarketDataLoader;

@Configuration
public class MarketConfiguration {

    @Bean
    public MarketDataCache marketDataCache() {
        return new MarketDataCache();
    }

    @Bean
    public MarketDataLoader marketDataLoader(
            ApplicationConfig config,
            BinanceFuturesClient client,
            HeikenAshiConverter heikenAshiConverter,
            BollingerBandCalculator bollingerBandCalculator,
            MarketDataCache cache) {

        return new MarketDataLoader(
                config,
                client,
                heikenAshiConverter,
                bollingerBandCalculator,
                cache);

    }

}