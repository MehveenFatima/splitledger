package com.mehveen.splitledger.split;

import com.mehveen.splitledger.exception.ValidationException;
import com.mehveen.splitledger.money.Money;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExactSplitStrategyTest {

    private final ExactSplitStrategy strategy = new ExactSplitStrategy();

    @Test
    void usesExactAmounts() {
        Map<String, Money> result = strategy.split(Money.ofRupees(1000),
                List.of(SplitShare.of("a", "650.50"), SplitShare.of("b", "349.50")));
        assertEquals(Money.parse("650.50"), result.get("a"));
        assertEquals(Money.parse("349.50"), result.get("b"));
    }

    @Test
    void rejectsAmountsThatDoNotAddUp() {
        ValidationException e = assertThrows(ValidationException.class, () -> strategy.split(Money.ofRupees(1000),
                List.of(SplitShare.of("a", "600"), SplitShare.of("b", "300"))));
        assertTrue(e.getMessage().contains("Rs 900.00"));
    }
}
