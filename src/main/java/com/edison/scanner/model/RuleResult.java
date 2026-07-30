package com.edison.scanner.model;

import java.util.Objects;

/**
 * Represents the result of evaluating a single trading rule.
 *
 * <p>
 * A rule result records whether the rule matched and provides a human-readable
 * explanation of the evaluation. This information is useful for debugging,
 * strategy analysis, logging, and user notifications.
 * </p>
 */
public final class RuleResult {

    /**
     * Rule name.
     */
    private final String ruleName;

    /**
     * Whether the rule matched.
     */
    private final boolean matched;

    /**
     * Human-readable explanation of the evaluation.
     */
    private final String reason;

    /**
     * Creates a rule result.
     *
     * @param ruleName
     *         Name of the evaluated rule.
     * @param matched
     *         Whether the rule matched.
     * @param reason
     *         Explanation of the evaluation result.
     */
    public RuleResult(
            String ruleName,
            boolean matched,
            String reason) {

        this.ruleName = Objects.requireNonNull(ruleName, "ruleName");
        this.reason = Objects.requireNonNull(reason, "reason");
        this.matched = matched;

    }

    /**
     * Returns the rule name.
     *
     * @return rule name.
     */
    public String getRuleName() {
        return ruleName;
    }

    /**
     * Returns whether the rule matched.
     *
     * @return {@code true} if matched; otherwise {@code false}.
     */
    public boolean isMatched() {
        return matched;
    }

    /**
     * Returns the explanation of the evaluation.
     *
     * @return evaluation reason.
     */
    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "RuleResult{" +
                "ruleName='" + ruleName + '\'' +
                ", matched=" + matched +
                ", reason='" + reason + '\'' +
                '}';
    }

}