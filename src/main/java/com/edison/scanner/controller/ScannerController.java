package com.edison.scanner.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edison.scanner.model.ScanExecutionResult;
import com.edison.scanner.model.ScanResult;
import com.edison.scanner.service.ScannerService;

@RestController
public class ScannerController {

    private final ScannerService scannerService;

    public ScannerController(
            ScannerService scannerService) {

        this.scannerService = scannerService;

    }

    @GetMapping("/api/scanner/run")
    public ScanExecutionResult run() {

        return scannerService.run();

    }

}