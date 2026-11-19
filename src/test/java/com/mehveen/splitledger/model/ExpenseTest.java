package com.mehveen.splitledger.model;

import com.mehveen.splitledger.exception.ValidationException;
import com.mehveen.splitledger.money.Money;
import com.mehveen.splitledger.split.SplitType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpenseTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 19);

    private static Map<String, Money> shares(String... pairs) {
        Map<String, Money> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], Money.parse(pairs[i + 1]));
        }
        return map;
    }

    @Test
    void storesFields() {
        Expense e = new Expense(1, "Dinner", "riya", Money.ofRupees(100), Category.FOOD,
                SplitType.EQUAL, shares("riya", "50", "sam", "50"), DAY);
        assertEquals(1, e.id());
        assertEquals("riya", e.payerId());
        assertEquals(Category.FOOD, e.category());
        assertEquals(DAY, e.date());
        assertEquals(2, e.shares().size());
    }

    @Test
    void descriptionIsTrimmedAndRequired() {
        Expense e = new Expense(1, "  Cab ", "riya", Money.ofRupees(10), null, null, shares("riya", "10"), DAY);
        assertEquals("Cab", e.description());
        assertThrows(ValidationException.class, () ->
                new Expense(2, " ", "riya", Money.ofRupees(10), null, null, shares("riya", "10"), DAY));
    }
}
