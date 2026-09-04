package com.edison.scanner.model.indicator;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a single RSI value.
 */
public final class Rsi {

    private final BigDecimal value;

    public Rsi(BigDecimal value) {

        this.value = Objects.requireNonNull(value);

    }

    public BigDecimal getValue() {
        return value;
    }

}