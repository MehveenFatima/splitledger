package com.mehveen.splitledger.split;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Looks up the {@link SplitStrategy} for a {@link SplitType}. */
public class SplitStrategyFactory {

    private final Map<SplitType, SplitStrategy> strategies = new EnumMap<>(SplitType.class);

    public SplitStrategyFactory() {
        this(List.of(
                new EqualSplitStrategy(),
                new ExactSplitStrategy(),
                new PercentSplitStrategy(),
                new SharesSplitStrategy()));
    }

    public SplitStrategyFactory(List<SplitStrategy> available) {
        for (SplitStrategy strategy : available) {
            strategies.put(strategy.type(), strategy);
        }
    }
}
