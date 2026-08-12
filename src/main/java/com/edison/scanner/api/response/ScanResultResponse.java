package com.edison.scanner.api.response;

import java.math.BigDecimal;

import com.edison.scanner.common.Timeframe;

public class ScanResultResponse {

    private String symbol;

    private Timeframe timeframe;

    private BigDecimal bbWidthPercent;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Timeframe getTimeframe() {
		return timeframe;
	}

	public void setTimeframe(Timeframe timeframe) {
		this.timeframe = timeframe;
	}

	public BigDecimal getBbWidthPercent() {
		return bbWidthPercent;
	}

	public void setBbWidthPercent(BigDecimal bbWidthPercent) {
		this.bbWidthPercent = bbWidthPercent;
	}

}