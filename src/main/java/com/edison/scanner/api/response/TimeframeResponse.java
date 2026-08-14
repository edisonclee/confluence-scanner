package com.edison.scanner.api.response;

import java.util.List;

import com.edison.scanner.common.Timeframe;

public class TimeframeResponse {

    private Timeframe timeframe;

    private List<ScanResultResponse> results;
    
    private String chartTimeframeDisplayName;

    private String bbTimeframeDisplayName;

	public String getChartTimeframeDisplayName() {
		return chartTimeframeDisplayName;
	}

	public void setChartTimeframeDisplayName(String chartTimeframeDisplayName) {
		this.chartTimeframeDisplayName = chartTimeframeDisplayName;
	}

	public String getBbTimeframeDisplayName() {
		return bbTimeframeDisplayName;
	}

	public void setBbTimeframeDisplayName(String bbTimeframeDisplayName) {
		this.bbTimeframeDisplayName = bbTimeframeDisplayName;
	}

	public Timeframe getTimeframe() {
		return timeframe;
	}

	public void setTimeframe(Timeframe timeframe) {
		this.timeframe = timeframe;
	}

	public List<ScanResultResponse> getResults() {
		return results;
	}

	public void setResults(List<ScanResultResponse> results) {
		this.results = results;
	}

}