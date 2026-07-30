package com.edison.scanner.rule;

import com.edison.scanner.model.MarketContext;
import com.edison.scanner.model.RuleResult;

/**
 * Defines a single trading rule that evaluates the current market context.
 *
 * <p>
 * Implementations should be stateless and evaluate exactly one condition.
 * Examples include:
 * <ul>
 *     <li>Bollinger Band touch</li>
 *     <li>RSI oversold</li>
 *     <li>MACD bullish crossover</li>
 *     <li>Trend confirmation</li>
 * </ul>
 *
 * <p>
 * Implementations must not calculate indicators or modify the supplied
 * {@link MarketContext}. They should only evaluate the context and return
 * a {@link RuleResult}.
 */
public interface ConfluenceRule {
	
	/**
     * Returns the display name of this rule.
     *
     * @return rule name
     */
    String getName();
    
    /**
     * Evaluates this rule against the supplied market context.
     *
     * @param context
     *         current market context
     *
     * @return evaluation result
     */
    RuleResult evaluate(
            MarketContext context);

}