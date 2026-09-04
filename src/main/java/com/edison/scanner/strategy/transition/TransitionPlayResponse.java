package com.edison.scanner.strategy.transition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public final class TransitionPlayResponse {

	private final LocalDateTime scanTime;
	private final double durationSeconds;
	private final int symbolsScanned;
	private final List<TransitionPlayResult> signals;

	public TransitionPlayResponse(LocalDateTime scanTime, List<TransitionPlayResult> signals, double durationSeconds,
			int symbolsScanned) {

		this.scanTime = Objects.requireNonNull(scanTime);
		this.durationSeconds = durationSeconds;
		this.symbolsScanned = symbolsScanned;
		this.signals = List.copyOf(signals);
	}

	public LocalDateTime getScanTime() {
		return scanTime;
	}

	public List<TransitionPlayResult> getSignals() {
		return signals;
	}

	public double getDurationSeconds() {
		return durationSeconds;
	}

	public int getSymbolsScanned() {
		return symbolsScanned;
	}

}