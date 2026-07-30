package com.edison.scanner.engine;

import com.edison.scanner.model.MarketContext;
import com.edison.scanner.model.RuleResult;
import com.edison.scanner.model.ScanResult;
import com.edison.scanner.rule.ConfluenceRule;
import com.edison.scanner.strategy.TradingStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Executes the configured trading rules and delegates the final trading
 * decision to a {@link TradingStrategy}.
 *
 * <p>
 * The strategy engine is responsible for orchestrating the evaluation process.
 * It does not contain any trading logic itself. Trading decisions are delegated
 * to the configured {@link TradingStrategy}.
 *
 * <p>
 * This class is immutable and thread-safe provided that the configured
 * rules and strategy are also thread-safe.
 */
public final class StrategyEngine {

    /**
     * Trading rules to evaluate.
     */
    private final List<ConfluenceRule> rules;

    /**
     * Trading strategy.
     */
    private final TradingStrategy strategy;

    /**
     * Creates a strategy engine.
     *
     * @param rules
     *         trading rules to evaluate
     * @param strategy
     *         trading strategy
     */
    public StrategyEngine(
            List<ConfluenceRule> rules,
            TradingStrategy strategy) {

        this.rules = List.copyOf(
                Objects.requireNonNull(
                        rules,
                        "rules"));

        this.strategy = Objects.requireNonNull(
                strategy,
                "strategy");

    }

    /**
     * Evaluates the supplied market context.
     *
     * @param context
     *         current market context
     *
     * @return scan result
     */
    public ScanResult evaluate(
            MarketContext context) {

        Objects.requireNonNull(
                context,
                "context");

        List<RuleResult> ruleResults =
                new ArrayList<>(rules.size());

        for (ConfluenceRule rule : rules) {

            ruleResults.add(
                    rule.evaluate(context));

        }

        return strategy.evaluate(
                context,
                List.copyOf(ruleResults));

    }

}