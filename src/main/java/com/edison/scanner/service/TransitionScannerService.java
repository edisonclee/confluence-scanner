package com.edison.scanner.service;

import org.springframework.stereotype.Service;

import com.edison.scanner.TransitionScannerEngine;
import com.edison.scanner.strategy.transition.TransitionPlayResponse;

@Service
public class TransitionScannerService {

    private final TransitionScannerEngine engine;

    public TransitionScannerService(TransitionScannerEngine engine) {
        this.engine = engine;
    }

    public TransitionPlayResponse run() {
        return engine.runTransitionScan();
    }

}