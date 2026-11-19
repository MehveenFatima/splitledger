package com.mehveen.splitledger.split;

import com.mehveen.splitledger.exception.ValidationException;
import com.mehveen.splitledger.money.Money;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Each participant owes an exact rupee amount; amounts must add up to the total. */
public class ExactSplitStrategy extends AbstractSplitStrategy {

    @Override
    public SplitType type() {
        return SplitType.EXACT;
    }

    @Override
    protected Map<String, Money> compute(Money total, List<SplitShare> shares) {
        Map<String, Money> result = new LinkedHashMap<>();
        Money sum = Money.ZERO;
        for (SplitShare share : shares) {
            Money amount = toMoney(share);
            result.put(share.memberId(), amount);
            sum = sum.plus(amount);
        }
        if (!sum.equals(total)) {
            throw new ValidationException(
                    "Exact amounts add up to " + sum.format() + " but the expense is " + total.format());
        }
        return result;
    }

    private static Money toMoney(SplitShare share) {
        if (share.value().signum() < 0) {
            throw new ValidationException("Amount for " + share.memberId() + " cannot be negative");
        }
        try {
            return Money.parse(share.value().toPlainString());
        } catch (IllegalArgumentException e) {
            throw new ValidationException(e.getMessage());
        }
    }
}
