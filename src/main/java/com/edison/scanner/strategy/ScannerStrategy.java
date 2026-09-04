package com.edison.scanner.strategy;

import java.util.List;

import com.edison.scanner.market.MarketData;
import com.edison.scanner.model.ScanResult;
import com.edison.scanner.model.ScannerRequest;

/**
 * Scanner strategy.
 */
public interface ScannerStrategy {

	/**
	 * Strategy name.
	 *
	 * @return strategy name
	 */
	String getName();

	/**
	 * Executes the scan.
	 *
	 * @param marketData prepared market data
	 * @param request    scanner request
	 *
	 * @return scan results
	 */
	List<ScanResult> scan(MarketData marketData, ScannerRequest request);

}