package com.mehveen.splitledger.split;

import com.mehveen.splitledger.exception.ValidationException;
import com.mehveen.splitledger.money.Money;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SharesSplitStrategyTest {

    private final SharesSplitStrategy strategy = new SharesSplitStrategy();

    @Test
    void splitsByRatio() {
        Map<String, Money> result = strategy.split(Money.ofRupees(30_000),
                List.of(SplitShare.of("a", "2"), SplitShare.of("b", "1"), SplitShare.of("c", "1")));
        assertEquals(Money.ofRupees(15_000), result.get("a"));
        assertEquals(Money.ofRupees(7_500), result.get("b"));
        assertEquals(Money.ofRupees(7_500), result.get("c"));
    }

    @Test
    void allowsFractionalShares() {
        Map<String, Money> result = strategy.split(Money.ofRupees(250),
                List.of(SplitShare.of("a", "1.5"), SplitShare.of("b", "1")));
        assertEquals(Money.ofRupees(150), result.get("a"));
        assertEquals(Money.ofRupees(100), result.get("b"));
    }

    @Test
    void rejectsZeroShares() {
        assertThrows(ValidationException.class, () -> strategy.split(Money.ofRupees(100),
                List.of(SplitShare.of("a", "1"), SplitShare.of("b", "0"))));
    }

    @Test
    void unevenRatiosAddUp() {
        Money total = Money.ofRupees(100);
        Map<String, Money> result = strategy.split(total,
                List.of(SplitShare.of("a", "1"), SplitShare.of("b", "1"), SplitShare.of("c", "1")));
        assertEquals(total, result.values().stream().reduce(Money.ZERO, Money::plus));
    }
}
