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
}
