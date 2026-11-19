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

class MarkdownReportTest {

    private final MarkdownReport report = new MarkdownReport();
    private Ledger ledger;

    @BeforeEach
    void setUp() {
        ledger = new Ledger();
        ledger.addMember(new Member("riya", "Riya"));
        ledger.addMember(new Member("sam", "Sam"));
    }

    private void addDinner() {
        ledger.addExpense("Dinner | drinks", "riya", Money.ofRupees(500), Category.FOOD, SplitType.EQUAL,
                List.of(SplitShare.of("riya"), SplitShare.of("sam")), LocalDate.of(2026, 11, 19));
    }

    @Test
    void startsWithTitleAndTotal() {
        addDinner();
        String md = report.build(ledger, "Goa trip");
        assertTrue(md.startsWith("# Goa trip\n"));
        assertTrue(md.contains("**Total spent:** Rs 500.00 across 1 expense(s)"));
    }

    @Test
    void hasBalanceAndExpenseTables() {
        addDinner();
        String md = report.build(ledger, "Trip");
        assertTrue(md.contains("| Riya | Rs 250.00 | Rs 250.00 |"));
        assertTrue(md.contains("| 1 | 2026-11-19 | Dinner \\| drinks | riya | Rs 500.00 | food |"));
    }

    @Test
    void settlementIsAChecklist() {
        addDinner();
        assertTrue(report.build(ledger, "Trip").contains("- [ ] sam pays riya Rs 250.00"));
    }

    @Test
    void emptyLedgerSkipsExpenseSections() {
        String md = report.build(ledger, "Empty");
        assertFalse(md.contains("## Expenses"));
        assertFalse(md.contains("## By category"));
        assertTrue(md.contains("Everyone is settled up."));
    }

    @Test
    void escapesTableCharacters() {
        assertEquals("a \\| b \\\\ c", MarkdownReport.escape("a | b \\ c"));
    }
}
