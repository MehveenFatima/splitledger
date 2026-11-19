package com.mehveen.splitledger.report;

import com.mehveen.splitledger.ledger.Ledger;
import com.mehveen.splitledger.model.Category;
import com.mehveen.splitledger.model.Expense;
import com.mehveen.splitledger.model.Member;
import com.mehveen.splitledger.money.Money;
import com.mehveen.splitledger.settle.DebtSimplifier;
import com.mehveen.splitledger.settle.Transfer;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Renders a ledger as a Markdown document that can be pasted into a chat,
 * a GitHub issue or a notes app.
 */
public class MarkdownReport {

    private final DebtSimplifier simplifier;

    public MarkdownReport() {
        this(new DebtSimplifier());
    }

    public MarkdownReport(DebtSimplifier simplifier) {
        this.simplifier = simplifier;
    }

    public String build(Ledger ledger, String title) {
        StringBuilder md = new StringBuilder();
        md.append("# ").append(escape(title)).append("\n\n");
        md.append("**Total spent:** ").append(ledger.totalSpent().format())
                .append(" across ").append(ledger.expenses().size()).append(" expense(s)\n\n");
        appendMembers(md, ledger);
        appendExpenses(md, ledger);
        appendCategories(md, ledger);
        appendSettlement(md, ledger);
        return md.toString();
    }

    private void appendMembers(StringBuilder md, Ledger ledger) {
        md.append("## Balances\n\n| Member | Spent | Balance |\n|---|---:|---:|\n");
        Map<String, Money> balances = ledger.balances();
        for (Member member : ledger.members()) {
            md.append("| ").append(escape(member.name())).append(" | ")
                    .append(ledger.spendingOf(member.id()).format()).append(" | ")
                    .append(balances.get(member.id()).format()).append(" |\n");
        }
        md.append('\n');
    }

    private void appendExpenses(StringBuilder md, Ledger ledger) {
        if (ledger.expenses().isEmpty()) {
            return;
        }
        md.append("## Expenses\n\n| # | Date | Description | Paid by | Amount | Category |\n")
                .append("|---:|---|---|---|---:|---|\n");
        for (Expense e : ledger.expenses()) {
            md.append("| ").append(e.id()).append(" | ").append(e.date()).append(" | ")
                    .append(escape(e.description())).append(" | ").append(e.payerId()).append(" | ")
                    .append(e.amount().format()).append(" | ").append(e.category().label()).append(" |\n");
        }
        md.append('\n');
    }

    private void appendCategories(StringBuilder md, Ledger ledger) {
        Map<Category, Money> totals = ledger.totalsByCategory();
        if (totals.isEmpty()) {
            return;
        }
        md.append("## By category\n\n");
        totals.entrySet().stream()
                .sorted(Map.Entry.<Category, Money>comparingByValue(Comparator.reverseOrder()))
                .forEach(e -> md.append("- **").append(e.getKey().label()).append("**: ")
                        .append(e.getValue().format()).append('\n'));
        md.append('\n');
    }

    private void appendSettlement(StringBuilder md, Ledger ledger) {
        md.append("## Settle up\n\n");
        List<Transfer> transfers = simplifier.simplify(ledger.balances());
        if (transfers.isEmpty()) {
            md.append("Everyone is settled up.\n");
            return;
        }
        transfers.forEach(t -> md.append("- [ ] ").append(t).append('\n'));
    }

    /** Escapes characters that would break a Markdown table cell. */
    static String escape(String text) {
        return text.replace("\\", "\\\\").replace("|", "\\|");
    }
}
