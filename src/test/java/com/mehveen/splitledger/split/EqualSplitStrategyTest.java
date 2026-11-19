package com.mehveen.splitledger.split;

import com.mehveen.splitledger.exception.ValidationException;
import com.mehveen.splitledger.money.Money;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EqualSplitStrategyTest {

    private final EqualSplitStrategy strategy = new EqualSplitStrategy();

    @Test
    void splitsEvenly() {
        Map<String, Money> result = strategy.split(Money.ofRupees(900),
                List.of(SplitShare.of("a"), SplitShare.of("b"), SplitShare.of("c")));
        assertEquals(Money.ofRupees(300), result.get("a"));
        assertEquals(Money.ofRupees(300), result.get("b"));
        assertEquals(Money.ofRupees(300), result.get("c"));
    }

    @Test
    void distributesLeftoverPaise() {
        Map<String, Money> result = strategy.split(Money.ofRupees(100),
                List.of(SplitShare.of("a"), SplitShare.of("b"), SplitShare.of("c")));
        assertEquals(Money.parse("33.34"), result.get("a"));
        assertEquals(Money.parse("33.33"), result.get("c"));
    }

    @Test
    void keepsParticipantOrder() {
        Map<String, Money> result = strategy.split(Money.ofRupees(10),
                List.of(SplitShare.of("zed"), SplitShare.of("amy")));
        assertEquals(List.of("zed", "amy"), List.copyOf(result.keySet()));
    }
}
