package com.mehveen.splitledger.split;

import com.mehveen.splitledger.exception.ValidationException;
import com.mehveen.splitledger.money.Money;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PercentSplitStrategyTest {

    private final PercentSplitStrategy strategy = new PercentSplitStrategy();

    @Test
    void splitsByPercent() {
        Map<String, Money> result = strategy.split(Money.ofRupees(2000),
                List.of(SplitShare.of("a", "50"), SplitShare.of("b", "30"), SplitShare.of("c", "20")));
        assertEquals(Money.ofRupees(1000), result.get("a"));
        assertEquals(Money.ofRupees(600), result.get("b"));
        assertEquals(Money.ofRupees(400), result.get("c"));
    }
}
