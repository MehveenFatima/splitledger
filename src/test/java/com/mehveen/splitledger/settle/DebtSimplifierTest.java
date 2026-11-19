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

    @Test
    void settledGroupNeedsNoTransfers() {
        assertTrue(simplifier.simplify(balances("a", 0, "b", 0)).isEmpty());
    }

    @Test
    void twoPeopleOneTransfer() {
        List<Transfer> transfers = simplifier.simplify(balances("a", 500, "b", -500));
        assertEquals(List.of(new Transfer("b", "a", Money.ofRupees(500))), transfers);
    }

    @Test
    void oneCreditorManyDebtors() {
        List<Transfer> transfers = simplifier.simplify(balances("a", 600, "b", -200, "c", -400));
        assertEquals(2, transfers.size());
        assertEquals(new Transfer("c", "a", Money.ofRupees(400)), transfers.get(0));
        assertEquals(new Transfer("b", "a", Money.ofRupees(200)), transfers.get(1));
    }

    @Test
    void oneDebtorManyCreditors() {
        List<Transfer> transfers = simplifier.simplify(balances("a", 100, "b", 300, "c", -400));
        assertEquals(List.of(
                new Transfer("c", "b", Money.ofRupees(300)),
                new Transfer("c", "a", Money.ofRupees(100))), transfers);
    }

    @Test
    void chainCollapses() {
        // a owes b 100 and b owes c 100 -> net: a -100, b 0, c +100 -> a pays c directly
        List<Transfer> transfers = simplifier.simplify(balances("a", -100, "b", 0, "c", 100));
        assertEquals(List.of(new Transfer("a", "c", Money.ofRupees(100))), transfers);
    }

    @Test
    void rejectsUnbalancedInput() {
        assertThrows(IllegalArgumentException.class, () -> simplifier.simplify(balances("a", 100, "b", -50)));
    }

    @Test
    void tiesAreDeterministic() {
        List<Transfer> first = simplifier.simplify(balances("x", 100, "a", 100, "m", -100, "b", -100));
        List<Transfer> second = simplifier.simplify(balances("b", -100, "m", -100, "a", 100, "x", 100));
        assertEquals(first, second);
        assertEquals(new Transfer("b", "a", Money.ofRupees(100)), first.get(0));
    }
}
