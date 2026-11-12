
package com.edison.scanner.model;

import java.util.List;

public class ScanExecutionResult {

	private final List<ScanResult> results;
	private final int symbolsScanned;
	private final int symbolsFailed;
	private final double downloadSeconds;
	private final double scanSeconds;
	private final double totalSeconds;

	public ScanExecutionResult(List<ScanResult> results, int symbolsScanned, int symbolsFailed, double downloadSeconds,
			double scanSeconds, double totalSeconds) {

		this.results = List.copyOf(results);
		this.symbolsScanned = symbolsScanned;
		this.symbolsFailed = symbolsFailed;
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

	public int getSymbolsFailed() {
		return symbolsFailed;
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
