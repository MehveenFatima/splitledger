package com.mehveen.splitledger.split;

import com.mehveen.splitledger.exception.ValidationException;
import com.mehveen.splitledger.money.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Each participant owes in proportion to their share count.
 * Example: rent ₹30,000 with shares 2:1:1 gives 15,000 / 7,500 / 7,500.
 */
public class SharesSplitStrategy extends AbstractSplitStrategy {

    @Override
    public SplitType type() {
        return SplitType.SHARES;
    }
}
