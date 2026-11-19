package com.mehveen.splitledger.settle;

import com.mehveen.splitledger.money.Money;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DebtSimplifierTest {

    private final DebtSimplifier simplifier = new DebtSimplifier();

    private static Map<String, Money> balances(Object... pairs) {
        Map<String, Money> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((String) pairs[i], Money.ofRupees(((Number) pairs[i + 1]).longValue()));
        }
        return map;
    }

    /** Applies transfers to the balances and returns what is left. */
    private static Map<String, Money> apply(Map<String, Money> balances, List<Transfer> transfers) {
        Map<String, Money> left = new HashMap<>(balances);
        for (Transfer t : transfers) {
            left.merge(t.fromId(), t.amount(), Money::plus);
            left.merge(t.toId(), t.amount().negate(), Money::plus);
        }
        return left;
    }

    @Test
    void emptyBalancesNeedNoTransfers() {
        assertTrue(simplifier.simplify(Map.of()).isEmpty());
    }
}
