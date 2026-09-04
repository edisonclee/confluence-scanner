package com.edison.scanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.edison.scanner.ScannerEngine;
import com.edison.scanner.TransitionScannerEngine;
import com.edison.scanner.bitunix.BitunixSymbolProvider;
import com.edison.scanner.detector.TouchDetector;
import com.edison.scanner.market.MarketDataCache;
import com.edison.scanner.market.MarketDataLoader;
import com.edison.scanner.strategy.ScannerStrategy;
import com.edison.scanner.strategy.impl.BollingerBandStrategy;
import com.edison.scanner.strategy.impl.TransitionPlayStrategy;

@Configuration
public class ScannerConfiguration {

	@Bean
	public BollingerBandStrategy bollingerBandStrategy(ApplicationConfig config, TouchDetector touchDetector) {

		return new BollingerBandStrategy(config, touchDetector);

	}

	@Bean
	public ScannerEngine scannerEngine(BitunixSymbolProvider symbolProvider, MarketDataLoader marketDataLoader,
			ScannerStrategy marketScanner, MarketDataCache cache, ApplicationConfig config) {

		return new ScannerEngine(symbolProvider, marketDataLoader, marketScanner, cache,
				config.getScannerDownloadThreads());

	}

	@Bean
	public TransitionScannerEngine transitionScannerEngine(BitunixSymbolProvider symbolProvider,
			MarketDataLoader marketDataLoader, MarketDataCache cache, TransitionPlayStrategy transitionPlayStrategy,
			ApplicationConfig config) {

		return new TransitionScannerEngine(symbolProvider, marketDataLoader, cache, transitionPlayStrategy,
				config.getScannerDownloadThreads());

	}

}