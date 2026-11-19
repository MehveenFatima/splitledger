package com.mehveen.splitledger.split;

import com.mehveen.splitledger.money.Money;

import java.util.List;
import java.util.Map;

/**
 * Divides the total equally. Leftover paise (e.g. ₹100 / 3) go to the
 * first participants so nothing is lost: 33.34, 33.33, 33.33.
 */
public class EqualSplitStrategy extends AbstractSplitStrategy {

    @Override
    public SplitType type() {
        return SplitType.EQUAL;
    }

    @Override
    protected Map<String, Money> compute(Money total, List<SplitShare> shares) {
        return zip(shares, total.allocate(shares.size()));
    }
}
