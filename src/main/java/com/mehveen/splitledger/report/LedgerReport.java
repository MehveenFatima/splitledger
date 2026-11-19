package com.mehveen.splitledger.report;

import com.mehveen.splitledger.ledger.Ledger;
import com.mehveen.splitledger.model.Category;
import com.mehveen.splitledger.model.Member;
import com.mehveen.splitledger.money.Money;
import com.mehveen.splitledger.settle.DebtSimplifier;
import com.mehveen.splitledger.settle.Transfer;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Builds a plain-text summary of a ledger: totals, balances, categories and settle-up plan. */
public class LedgerReport {

    private final DebtSimplifier simplifier;

    public LedgerReport() {
        this(new DebtSimplifier());
    }

    public LedgerReport(DebtSimplifier simplifier) {
        this.simplifier = simplifier;
    }

    public String build(Ledger ledger) {
        StringBuilder out = new StringBuilder();
        out.append("=== SplitLedger report ===\n");
        out.append("Members: ").append(ledger.members().size())
                .append(" | Expenses: ").append(ledger.expenses().size())
                .append(" | Total spent: ").append(ledger.totalSpent().format()).append('\n');
        appendBalances(out, ledger);
        appendCategories(out, ledger);
        appendSettlement(out, ledger);
        return out.toString();
    }
}
