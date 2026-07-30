package com.edison.scanner.rule;

import com.edison.scanner.model.MarketContext;
import com.edison.scanner.model.RuleResult;

/**
 * Trading rule that evaluates whether the current candle touched any
 * Bollinger Band.
 *
 * <p>
 * This rule does not perform Bollinger Band calculations or touch detection.
 * It only evaluates the results already present in the supplied
 * {@link MarketContext}.
 */
public final class BollingerTouchRule
        implements ConfluenceRule {

    /**
     * Display name of this rule.
     */
    private static final String NAME = "Bollinger Touch";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public RuleResult evaluate(
            MarketContext context) {

        boolean matched =
                !context.getTouchResults().isEmpty();

        String reason;

        if (matched) {

            reason = String.format(
                    "%d Bollinger Band touch(es) detected.",
                    context.getTouchResults().size());

        } else {

            reason = "No Bollinger Band touch detected.";

        }

        return new RuleResult(
                getName(),
                matched,
                reason);

    }

}