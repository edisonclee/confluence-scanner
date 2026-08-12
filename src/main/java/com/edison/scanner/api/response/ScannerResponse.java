package com.edison.scanner.api.response;

import java.time.LocalDateTime;
import java.util.List;


public class ScannerResponse {

    private LocalDateTime generatedAt;

    private double downloadSeconds;

    private double scanSeconds;

    private double totalSeconds;

    private int symbolsScanned;

    private int totalMatches;

    private int multiTimeframeMatches;
    
    private List<MultiTimeframeResponse> multiTimeframe;

    private List<TimeframeResponse> timeframes;

	public LocalDateTime getGeneratedAt() {
		return generatedAt;
	}

	public void setGeneratedAt(LocalDateTime generatedAt) {
		this.generatedAt = generatedAt;
	}

	public double getDownloadSeconds() {
		return downloadSeconds;
	}

	public void setDownloadSeconds(double downloadSeconds) {
		this.downloadSeconds = downloadSeconds;
	}

	public double getScanSeconds() {
		return scanSeconds;
	}

	public void setScanSeconds(double scanSeconds) {
		this.scanSeconds = scanSeconds;
	}

	public double getTotalSeconds() {
		return totalSeconds;
	}

	public void setTotalSeconds(double totalSeconds) {
		this.totalSeconds = totalSeconds;
	}

	public int getSymbolsScanned() {
		return symbolsScanned;
	}

	public void setSymbolsScanned(int symbolsScanned) {
		this.symbolsScanned = symbolsScanned;
	}

	public int getTotalMatches() {
		return totalMatches;
	}

	public void setTotalMatches(int totalMatches) {
		this.totalMatches = totalMatches;
	}

	public List<MultiTimeframeResponse> getMultiTimeframe() {
		return multiTimeframe;
	}

	public void setMultiTimeframe(List<MultiTimeframeResponse> multiTimeframe) {
		this.multiTimeframe = multiTimeframe;
	}

	public List<TimeframeResponse> getTimeframes() {
		return timeframes;
	}

	public void setTimeframes(List<TimeframeResponse> timeframes) {
		this.timeframes = timeframes;
	}
	
	public int getMultiTimeframeMatches() {
	    return multiTimeframeMatches;
	}

	public void setMultiTimeframeMatches(
	        int multiTimeframeMatches) {

	    this.multiTimeframeMatches =
	            multiTimeframeMatches;

	}

}