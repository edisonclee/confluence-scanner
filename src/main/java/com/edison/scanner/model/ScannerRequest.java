package com.edison.scanner.model;

import java.util.EnumSet;
import java.util.Set;

import com.edison.scanner.common.Timeframe;

public final class ScannerRequest {

	private final Set<Timeframe> bbTimeframes;

	public ScannerRequest(Set<Timeframe> bbTimeframes) {

		if (bbTimeframes == null || bbTimeframes.isEmpty()) {
			throw new IllegalArgumentException("At least one BB timeframe must be selected.");
		}

		this.bbTimeframes = EnumSet.copyOf(bbTimeframes);

	}

	public Set<Timeframe> getBbTimeframes() {
		return Set.copyOf(bbTimeframes);
	}

}