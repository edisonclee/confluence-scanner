package com.edison.scanner.model;

import com.edison.scanner.common.ScanDirection;
import com.edison.scanner.common.Timeframe;

import java.util.List;
import java.util.Objects;

/**
 * Represents the outcome of evaluating a market against the configured
 * trading strategy.
 *
 * <p>
 * A {@code ScanResult} contains the information required to report or act
 * upon a detected trading opportunity. It intentionally contains only the
 * final decision rather than the complete market context.
 */
public final class ScanResult {

    /**
     * Trading symbol.
     */
    private final String symbol;

    /**
     * Chart timeframe.
     */
    private final Timeframe timeframe;

    /**
     * Trade direction.
     */
    private final ScanDirection direction;

    /**
     * Rules that matched.
     */
    private final List<RuleResult> matchedRules;

    /**
     * Creates a scan result.
     *
     * @param symbol
     *         trading symbol
     * @param timeframe
     *         chart timeframe
     * @param direction
     *         detected trade direction
     * @param matchedRules
     *         matched trading rules
     */
    public ScanResult(
            String symbol,
            Timeframe timeframe,
            ScanDirection direction,
            List<RuleResult> matchedRules) {

        this.symbol = Objects.requireNonNull(symbol, "symbol");
        this.timeframe = Objects.requireNonNull(timeframe, "timeframe");
        this.direction = Objects.requireNonNull(direction, "direction");
        this.matchedRules = List.copyOf(
                Objects.requireNonNull(
                        matchedRules,
                        "matchedRules"));
    }

    /**
     * Returns the trading symbol.
     *
     * @return trading symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the chart timeframe.
     *
     * @return timeframe
     */
    public Timeframe getTimeframe() {
        return timeframe;
    }

    /**
     * Returns the detected trade direction.
     *
     * @return trade direction
     */
    public ScanDirection getDirection() {
        return direction;
    }

    /**
     * Returns the matched trading rules.
     *
     * @return immutable list of matched rules
     */
    public List<RuleResult> getMatchedRules() {
        return matchedRules;
    }

}