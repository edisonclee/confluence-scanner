package com.edison.scanner.service;

import org.springframework.stereotype.Service;

import com.edison.scanner.ScannerEngine;
import com.edison.scanner.model.ScanExecutionResult;

@Service
public class ScannerService {

    private final ScannerEngine scannerEngine;

    public ScannerService(
            ScannerEngine scannerEngine) {

        this.scannerEngine = scannerEngine;

    }

    public ScanExecutionResult run() {

        return scannerEngine.run();

    }

}