package com.mehveen.splitledger.split;

import com.mehveen.splitledger.exception.ValidationException;
import com.mehveen.splitledger.money.Money;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Template method: shared validation for every split strategy, so each
 * concrete strategy only implements the arithmetic.
 */
public abstract class AbstractSplitStrategy implements SplitStrategy {

    @Override
    public final Map<String, Money> split(Money total, List<SplitShare> shares) {
        Objects.requireNonNull(total, "total");
        if (!total.isPositive()) {
            throw new ValidationException("Expense amount must be greater than zero");
        }
        if (shares == null || shares.isEmpty()) {
            throw new ValidationException("At least one participant is required");
        }
        rejectDuplicates(shares);
        Map<String, Money> result = compute(total, shares);
        verifyTotal(total, result);
        return Collections.unmodifiableMap(result);
    }

    protected abstract Map<String, Money> compute(Money total, List<SplitShare> shares);

    private static void rejectDuplicates(List<SplitShare> shares) {
        Set<String> seen = new HashSet<>();
        for (SplitShare share : shares) {
            if (!seen.add(share.memberId())) {
                throw new ValidationException("Duplicate participant: " + share.memberId());
            }
        }
    }

    private static void verifyTotal(Money total, Map<String, Money> parts) {
        Money sum = parts.values().stream().reduce(Money.ZERO, Money::plus);
        if (!sum.equals(total)) {
            throw new IllegalStateException("Split parts add up to " + sum + " instead of " + total);
        }
    }
}
