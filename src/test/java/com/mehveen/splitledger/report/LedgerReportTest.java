package com.mehveen.splitledger.report;

import com.mehveen.splitledger.ledger.Ledger;
import com.mehveen.splitledger.model.Category;
import com.mehveen.splitledger.model.Member;
import com.mehveen.splitledger.money.Money;
import com.mehveen.splitledger.split.SplitShare;
import com.mehveen.splitledger.split.SplitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LedgerReportTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 19);

    private final LedgerReport report = new LedgerReport();
    private Ledger ledger;

    @BeforeEach
    void setUp() {
        ledger = new Ledger();
        ledger.addMember(new Member("riya", "Riya"));
        ledger.addMember(new Member("sam", "Sam"));
    }

    @Test
    void headerShowsTotals() {
        ledger.addExpense("Dinner", "riya", Money.ofRupees(500), Category.FOOD, SplitType.EQUAL,
                List.of(SplitShare.of("riya"), SplitShare.of("sam")), DAY);
        String text = report.build(ledger);
        assertTrue(text.contains("Members: 2 | Expenses: 1 | Total spent: Rs 500.00"));
    }

    @Test
    void emptyLedgerIsSettled() {
        String text = report.build(ledger);
        assertTrue(text.contains("Everyone is settled up."));
        assertFalse(text.contains("By category"));
    }

    @Test
    void listsSettlement() {
        ledger.addExpense("Dinner", "riya", Money.ofRupees(500), Category.FOOD, SplitType.EQUAL,
                List.of(SplitShare.of("riya"), SplitShare.of("sam")), DAY);
        assertTrue(report.build(ledger).contains("sam pays riya Rs 250.00"));
    }

    @Test
    void categoriesLargestFirst() {
        ledger.addExpense("Snacks", "riya", Money.ofRupees(50), Category.FOOD, SplitType.EQUAL,
                List.of(SplitShare.of("riya")), DAY);
        ledger.addExpense("Rent", "sam", Money.ofRupees(950), Category.RENT, SplitType.EQUAL,
                List.of(SplitShare.of("sam")), DAY);
        String text = report.build(ledger);
        assertTrue(text.indexOf("rent") < text.indexOf("food"));
        assertTrue(text.contains("(95%)"));
    }

    @Test
    void percentOfRoundsAndHandlesZero() {
        assertEquals(33L, LedgerReport.percentOf(Money.ofRupees(1), Money.ofRupees(3)));
        assertEquals(67L, LedgerReport.percentOf(Money.ofRupees(2), Money.ofRupees(3)));
        assertEquals(0L, LedgerReport.percentOf(Money.ZERO, Money.ZERO));
    }
}
