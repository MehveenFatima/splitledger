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

    private void appendBalances(StringBuilder out, Ledger ledger) {
        out.append("\nBalances (+ is owed, - owes):\n");
        Map<String, Money> balances = ledger.balances();
        for (Member member : ledger.members()) {
            out.append(String.format("  %-12s spent %12s   balance %12s%n",
                    member.id(), ledger.spendingOf(member.id()).format(), balances.get(member.id()).format()));
        }
    }

    private void appendCategories(StringBuilder out, Ledger ledger) {
        Map<Category, Money> totals = ledger.totalsByCategory();
        if (totals.isEmpty()) {
            return;
        }
        out.append("\nBy category:\n");
        totals.entrySet().stream()
                .sorted(Map.Entry.<Category, Money>comparingByValue(Comparator.reverseOrder()))
                .forEach(e -> out.append(String.format("  %-14s %12s  (%s%%)%n",
                        e.getKey().label(), e.getValue().format(), percentOf(e.getValue(), ledger.totalSpent()))));
    }

    static long percentOf(Money part, Money total) {
        if (total.isZero()) {
            return 0;
        }
        return Math.round(part.paise() * 100.0 / total.paise());
    }

    private void appendSettlement(StringBuilder out, Ledger ledger) {
        List<Transfer> transfers = simplifier.simplify(ledger.balances());
        out.append("\nSettle up:\n");
        if (transfers.isEmpty()) {
            out.append("  Everyone is settled up.\n");
            return;
        }
        transfers.forEach(t -> out.append("  ").append(t).append('\n'));
    }
}
