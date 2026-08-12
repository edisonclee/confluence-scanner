package com.edison.scanner.strategy;

import java.util.List;

import com.edison.scanner.market.MarketData;
import com.edison.scanner.model.ScanResult;

public interface ScannerStrategy {

    /**
     * Strategy identifier.
     */
    String getName();

    /**
     * Executes the scan.
     */
    List<ScanResult> scan(
            MarketData marketData);

}