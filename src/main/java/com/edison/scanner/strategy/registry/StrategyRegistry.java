package com.edison.scanner.strategy.registry;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.edison.scanner.strategy.ScannerStrategy;

@Component
public class StrategyRegistry {

    private final Map<String, ScannerStrategy> strategies;

    public StrategyRegistry(
            List<ScannerStrategy> strategies) {

        this.strategies =
                strategies.stream()
                        .collect(Collectors.toMap(
                                ScannerStrategy::getName,
                                Function.identity()));

    }

    public ScannerStrategy get(
            String name) {

        return strategies.get(name);

    }

    public Map<String, ScannerStrategy> getStrategies() {
        return strategies;
    }

}