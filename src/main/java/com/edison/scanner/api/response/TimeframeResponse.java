package com.edison.scanner.api.response;

import java.util.List;

import com.edison.scanner.common.Timeframe;

public class TimeframeResponse {

    private Timeframe timeframe;

    private List<ScanResultResponse> results;

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