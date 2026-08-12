package com.edison.scanner.model;

import java.util.List;

public class ScanExecutionResult {

    private final List<ScanResult> results;

    private final int symbolsScanned;

    private final double downloadSeconds;

    private final double scanSeconds;

    private final double totalSeconds;

    public ScanExecutionResult(
            List<ScanResult> results,
            int symbolsScanned,
            double downloadSeconds,
            double scanSeconds,
            double totalSeconds) {

        this.results = List.copyOf(results);
        this.symbolsScanned = symbolsScanned;
        this.downloadSeconds = downloadSeconds;
        this.scanSeconds = scanSeconds;
        this.totalSeconds = totalSeconds;

    }

    public List<ScanResult> getResults() {
        return results;
    }

    public int getSymbolsScanned() {
        return symbolsScanned;
    }

    public double getDownloadSeconds() {
        return downloadSeconds;
    }

    public double getScanSeconds() {
        return scanSeconds;
    }

    public double getTotalSeconds() {
        return totalSeconds;
    }

}