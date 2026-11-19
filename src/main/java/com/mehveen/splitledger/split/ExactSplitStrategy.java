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
}
