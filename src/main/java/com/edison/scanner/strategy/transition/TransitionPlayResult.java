package com.edison.scanner.strategy.transition;

import java.util.Objects;

public final class TransitionPlayResult {

    private final String symbol;

    private final TransitionDirection direction;

    private final String pattern;

    public TransitionPlayResult(
            String symbol,
            TransitionDirection direction,
            String pattern) {

        this.symbol = Objects.requireNonNull(symbol);
        this.direction = Objects.requireNonNull(direction);
        this.pattern = Objects.requireNonNull(pattern);

    }

    public String getSymbol() {
        return symbol;
    }

    public TransitionDirection getDirection() {
        return direction;
    }

    public String getPattern() {
        return pattern;
    }

}