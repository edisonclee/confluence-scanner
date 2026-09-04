package com.edison.scanner.service;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.edison.scanner.ScannerEngine;
import com.edison.scanner.model.ScanExecutionResult;
import com.edison.scanner.model.ScannerRequest;

@Service
public class StrategyScannerService {

	private final ScannerEngine scannerEngine;

	public StrategyScannerService(ScannerEngine scannerEngine) {
		this.scannerEngine = Objects.requireNonNull(scannerEngine);
	}

	public ScanExecutionResult run(ScannerRequest request) {
		return scannerEngine.run(request);
	}

}