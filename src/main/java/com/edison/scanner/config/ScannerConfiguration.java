package com.edison.scanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.edison.scanner.ScannerEngine;
import com.edison.scanner.bitunix.BitunixSymbolProvider;
import com.edison.scanner.detector.TouchDetector;
import com.edison.scanner.market.MarketDataLoader;
import com.edison.scanner.strategy.impl.BollingerBandStrategy;

@Configuration
public class ScannerConfiguration {

	@Bean
	public BollingerBandStrategy bollingerBandStrategy(
	        ApplicationConfig config,
	        TouchDetector touchDetector) {

	    return new BollingerBandStrategy(
	            config,
	            touchDetector);

	}

    @Bean
    public ScannerEngine scannerEngine(
            BitunixSymbolProvider symbolProvider,
            MarketDataLoader marketDataLoader,
            BollingerBandStrategy marketScanner,
            ApplicationConfig config) {

        return new ScannerEngine(
                symbolProvider,
                marketDataLoader,
                marketScanner,
                config.getScannerDownloadThreads());

    }

}