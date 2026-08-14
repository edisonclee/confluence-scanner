package com.edison.scanner.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edison.scanner.api.mapper.ScannerResponseMapper;
import com.edison.scanner.api.response.ScannerResponse;
import com.edison.scanner.model.ScanExecutionResult;
import com.edison.scanner.service.ScannerService;

@RestController
@RequestMapping("/api/scanner")
public class ScannerController {

    private final ScannerService scannerService;

    private final ScannerResponseMapper mapper;

    public ScannerController(
            ScannerService scannerService,
            ScannerResponseMapper mapper) {

        this.scannerService = scannerService;
        this.mapper = mapper;

    }

    @GetMapping("/run")
    public ScannerResponse run() {

        ScanExecutionResult result =
                scannerService.run();

        return mapper.toResponse(result);

    }

}