package com.edison.scanner.strategy;

import com.edison.scanner.common.ScanDirection;
import com.edison.scanner.model.MarketContext;
import com.edison.scanner.model.RuleResult;
import com.edison.scanner.model.ScanResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Trading strategy for mean reversion setups.
 *
 * <p>
 * The current implementation requires all configured rules to match before
 * producing a trading signal.
 *
 * <p>
 * This implementation intentionally contains no indicator-specific logic.
 * Indicator calculations and rule evaluation are delegated to individual
 * {@code ConfluenceRule} implementations.
 */
public final class MeanReversionStrategy
        implements TradingStrategy {

    @Override
    public ScanResult evaluate(
            MarketContext context,
            List<RuleResult> ruleResults) {

        Objects.requireNonNull(
                context,
                "context");

        Objects.requireNonNull(
                ruleResults,
                "ruleResults");

        List<RuleResult> matchedRules =
                new ArrayList<>(ruleResults.size());

        boolean matched = true;

        for (RuleResult ruleResult : ruleResults) {

            if (ruleResult.isMatched()) {

                matchedRules.add(ruleResult);

            } else {

                matched = false;

            }

        }

        ScanDirection direction =
                matched
                        ? ScanDirection.LONG
                        : ScanDirection.NONE;

        return new ScanResult(
                context.getCandle().getSymbol(),
                context.getCandle().getTimeframe(),
                direction,
                matchedRules);

    }

}