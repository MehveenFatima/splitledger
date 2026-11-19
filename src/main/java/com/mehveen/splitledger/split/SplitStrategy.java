package com.mehveen.splitledger.split;

import com.mehveen.splitledger.money.Money;

import java.util.List;
import java.util.Map;

/**
 * Strategy pattern: each implementation knows how to divide a total
 * between participants for one {@link SplitType}.
 */
public interface SplitStrategy {

    SplitType type();

    /**
     * @return amount owed by each participant, in input order; always adds up to {@code total}
     */
    Map<String, Money> split(Money total, List<SplitShare> shares);
}
