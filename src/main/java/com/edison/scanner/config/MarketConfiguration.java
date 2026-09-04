package com.edison.scanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.edison.scanner.converter.HeikenAshiConverter;
import com.edison.scanner.indicator.BollingerBandCalculator;
import com.edison.scanner.indicator.RsiCalculator;
import com.edison.scanner.mapper.BitunixFuturesCandleMapper;
import com.edison.scanner.mapper.BitunixFuturesClient;
import com.edison.scanner.market.MarketDataCache;
import com.edison.scanner.market.MarketDataLoader;

@Configuration
public class MarketConfiguration {

    @Bean
    public MarketDataCache marketDataCache() {

        return new MarketDataCache();

    }

    @Bean
    public BitunixFuturesClient bitunixFuturesClient(
            ApplicationConfig config) {

        return new BitunixFuturesClient(
                config,
                new BitunixFuturesCandleMapper());

    }

    @Bean
    public MarketDataLoader marketDataLoader(
            ApplicationConfig config,
            BitunixFuturesClient client,
            HeikenAshiConverter heikenAshiConverter,
            BollingerBandCalculator bollingerBandCalculator,
            RsiCalculator rsiCalculator,
            MarketDataCache cache) {

        return new MarketDataLoader(
                config,
                client,
                heikenAshiConverter,
                bollingerBandCalculator,
                rsiCalculator,
                cache);

    }

}