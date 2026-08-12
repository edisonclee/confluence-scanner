package com.edison.scanner.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edison.scanner.service.ScannerService;

@RestController
public class ScannerController {

    private final ScannerService scannerService;

    public ScannerController(
            ScannerService scannerService) {

        this.scannerService = scannerService;

    }

    @GetMapping("/api/ping")
    public String ping() {

        return scannerService.health();

    }

}