package com.mehveen.splitledger.split;

import com.mehveen.splitledger.exception.ValidationException;
import com.mehveen.splitledger.money.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Each participant owes a percentage of the total; percentages must add up to 100. */
public class PercentSplitStrategy extends AbstractSplitStrategy {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    @Override
    public SplitType type() {
        return SplitType.PERCENT;
    }

    private static List<BigDecimal> validatedPercents(List<SplitShare> shares) {
        List<BigDecimal> percents = new ArrayList<>();
        BigDecimal sum = BigDecimal.ZERO;
        for (SplitShare share : shares) {
            if (share.value().signum() <= 0) {
                throw new ValidationException("Percent for " + share.memberId() + " must be greater than zero");
            }
            percents.add(share.value());
            sum = sum.add(share.value());
        }
        if (sum.compareTo(HUNDRED) != 0) {
            throw new ValidationException("Percentages add up to " + sum.toPlainString() + " instead of 100");
        }
        return percents;
    }

    @Override
    protected Map<String, Money> compute(Money total, List<SplitShare> shares) {
        return zip(shares, total.allocate(validatedPercents(shares)));
    }
}
