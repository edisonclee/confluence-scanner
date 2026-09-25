package com.edison.scanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.edison.scanner.bitunix.BitunixClient;
import com.edison.scanner.bitunix.BitunixSymbolProvider;
import com.edison.scanner.bitunix.BitunixUniverseService;
import com.edison.scanner.bitunix.mapper.BitunixSymbolMapper;
import com.edison.scanner.exchange.BinanceFuturesSymbolProvider;
import com.edison.scanner.mapper.BinanceFuturesCandleMapper;
import com.edison.scanner.mapper.BinanceFuturesClient;

@Configuration
public class ExchangeConfiguration {

	@Bean
	public BinanceFuturesCandleMapper binanceFuturesCandleMapper() {

		return new BinanceFuturesCandleMapper();
	}

	@Bean
	public BitunixSymbolMapper bitunixSymbolMapper() {

		return new BitunixSymbolMapper();
	}

	@Bean
	public BinanceFuturesClient binanceFuturesClient(ApplicationConfig config, BinanceFuturesCandleMapper mapper) {

		return new BinanceFuturesClient(config, mapper);
	}

	@Bean
	public BinanceFuturesSymbolProvider futuresSymbolProvider(BinanceFuturesClient client) {

		return new BinanceFuturesSymbolProvider(client);
	}

	@Bean
	public BitunixClient bitunixClient(ApplicationConfig config) {

		return new BitunixClient(config);
	}

	@Bean
	public BitunixSymbolProvider bitunixSymbolProvider(BitunixUniverseService universeService) {

		return new BitunixSymbolProvider(universeService);
	}
}