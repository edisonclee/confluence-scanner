package com.edison.scanner.service;

import org.springframework.stereotype.Service;

/**
 * Executes scanner operations.
 */
@Service
public class ScannerService {

    public String health() {
        return "Scanner Service OK";
    }

}