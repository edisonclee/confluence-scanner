package com.edison.scanner.strategy;

import com.edison.scanner.model.MarketContext;
import com.edison.scanner.model.RuleResult;
import com.edison.scanner.model.ScanResult;

import java.util.List;

/**
 * Defines a trading strategy.
 *
 * <p>
 * A trading strategy interprets the results produced by one or more
 * {@link com.edison.scanner.rule.ConfluenceRule}s and determines whether the
 * current market satisfies the conditions for a trading opportunity.
 *
 * <p>
 * Implementations should be stateless and thread-safe.
 */
public interface TradingStrategy {

    /**
     * Evaluates the supplied market context and rule results.
     *
     * @param context
     *         current market context
     * @param ruleResults
     *         results produced by all evaluated rules
     *
     * @return trading decision
     */
    ScanResult evaluate(
            MarketContext context,
            List<RuleResult> ruleResults);

}