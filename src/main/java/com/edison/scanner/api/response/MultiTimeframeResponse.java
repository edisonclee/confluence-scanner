package com.edison.scanner.api.response;

import java.util.List;

import com.edison.scanner.common.Timeframe;

public class MultiTimeframeResponse {

    private String symbol;

    private List<Timeframe> timeframes;
    
    private double largestBbWidthPercent;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public List<Timeframe> getTimeframes() {
		return timeframes;
	}

	public void setTimeframes(List<Timeframe> timeframes) {
		this.timeframes = timeframes;
	}

	public double getLargestBbWidthPercent() {
		return largestBbWidthPercent;
	}

	public void setLargestBbWidthPercent(double largestBbWidthPercent) {
		this.largestBbWidthPercent = largestBbWidthPercent;
	}

}