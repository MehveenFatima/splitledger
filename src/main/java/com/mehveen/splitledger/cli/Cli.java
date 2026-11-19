package com.mehveen.splitledger.cli;

import com.mehveen.splitledger.exception.LedgerException;
import com.mehveen.splitledger.exception.ValidationException;
import com.mehveen.splitledger.io.LedgerStore;
import com.mehveen.splitledger.ledger.Ledger;
import com.mehveen.splitledger.model.Category;
import com.mehveen.splitledger.model.Expense;
import com.mehveen.splitledger.model.Member;
import com.mehveen.splitledger.model.Payment;
import com.mehveen.splitledger.money.Money;
import com.mehveen.splitledger.report.LedgerReport;
import com.mehveen.splitledger.settle.DebtSimplifier;
import com.mehveen.splitledger.settle.Transfer;
import com.mehveen.splitledger.split.SplitShare;
import com.mehveen.splitledger.split.SplitType;

import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Interactive command interpreter. Each call to {@link #execute(String)} runs one
 * command and prints the result, so it is easy to test without a real console.
 */
public class Cli {

    private final PrintStream out;
    private final Clock clock;
    private final LedgerStore store;
    private final DebtSimplifier simplifier = new DebtSimplifier();
    private Ledger ledger;

    public Cli(Ledger ledger, PrintStream out, Clock clock) {
        this(ledger, out, clock, new LedgerStore());
    }

    public Cli(Ledger ledger, PrintStream out, Clock clock, LedgerStore store) {
        this.ledger = ledger;
        this.out = out;
        this.clock = clock;
        this.store = store;
    }

    public Ledger ledger() {
        return ledger;
    }

    /** Runs one command line. Returns {@code false} when the user asked to exit. */
    public boolean execute(String line) {
        try {
            ParsedCommand command = CommandParser.parse(line);
            if (command.isEmpty()) {
                return true;
            }
            return dispatch(command);
        } catch (LedgerException | IllegalArgumentException | UncheckedIOException e) {
            out.println("Error: " + e.getMessage());
            return true;
        }
    }

    private boolean dispatch(ParsedCommand command) {
        String name = command.word(0).toLowerCase();
        String sub = command.optionalWord(1).map(String::toLowerCase).orElse("");
        switch (name) {
            case "help" -> printHelp();
            case "member" -> member(sub, command);
            case "members" -> listMembers();
            case "expense" -> expense(sub, command);
            case "expenses" -> listExpenses(command);
            case "pay" -> pay(command);
            case "balances" -> balances();
            case "settle" -> settle();
            case "report" -> out.print(new LedgerReport(simplifier).build(ledger));
            case "stats" -> stats();
            case "save" -> save(command);
            case "load" -> load(command);
            case "exit", "quit" -> {
                out.println("Bye!");
                return false;
            }
            default -> out.println("Unknown command '" + name + "'. Type 'help' to see all commands.");
        }
        return true;
    }

    private void printHelp() {
        out.println("""
                Commands:
                  member add <name> [--id <handle>]       add a person to the group
                  member remove <id>                      remove a person with no history
                  members                                 list members and balances
                  expense add <description> <amount> --payer <id>
                        [--with a,b,c | a:300,b:200] [--split equal|exact|percent|shares]
                        [--category food|travel|rent|utilities|shopping|entertainment|other]
                        [--date YYYY-MM-DD]               record an expense
                  expense remove <id>                     delete an expense
                  expenses [--member id] [--category c] [--from date] [--to date]
                  pay <from> <to> <amount> [--date d]     record a settle-up payment
                  balances                                who owes and who is owed
                  settle                                  fewest payments to settle everyone
                  report                                  full summary
                  stats                                   quick spending statistics
                  save <file> / load <file>               store or restore the ledger
                  exit                                    quit""");
    }

    private void member(String sub, ParsedCommand command) {
        switch (sub) {
            case "add" -> {
                String name = command.word(2);
                Member member = command.option("id")
                        .map(id -> new Member(id, name))
                        .orElseGet(() -> Member.of(name));
                ledger.addMember(member);
                out.println("Added " + member.name() + " (id: " + member.id() + ")");
            }
            case "remove" -> {
                Member removed = ledger.removeMember(command.word(2));
                out.println("Removed " + removed.name());
            }
            default -> throw new ValidationException("Use 'member add <name>' or 'member remove <id>'");
        }
    }

    private void listMembers() {
        if (ledger.members().isEmpty()) {
            out.println("No members yet. Add one with: member add <name>");
            return;
        }
        Map<String, Money> balances = ledger.balances();
        for (Member member : ledger.members()) {
            out.printf("%-12s %-20s %12s%n", member.id(), member.name(), balances.get(member.id()).format());
        }
    }

    private void expense(String sub, ParsedCommand command) {
        switch (sub) {
            case "add" -> addExpense(command);
            case "remove" -> {
                Expense removed = ledger.removeExpense(parseId(command.word(2)));
                out.println("Removed expense " + removed);
            }
            default -> throw new ValidationException("Use 'expense add ...' or 'expense remove <id>'");
        }
    }

    private void addExpense(ParsedCommand command) {
        String description = command.word(2);
        Money amount = Money.parse(command.word(3));
        String payer = command.requireOption("payer");
        SplitType type = SplitType.fromString(command.option("split").orElse("equal"));
        List<SplitShare> shares = parseShares(command.option("with").orElse(""), type);
        Category category = Category.fromString(command.option("category").orElse("other"));
        LocalDate date = command.option("date").map(Cli::parseDate).orElse(LocalDate.now(clock));
        Expense expense = ledger.addExpense(description, payer, amount, category, type, shares, date);
        out.println("Added expense " + expense);
        expense.shares().forEach((id, share) -> out.println("  " + id + " owes " + share.format()));
    }

    /**
     * Parses {@code a,b,c} (equal split) or {@code a:300,b:200} (other splits).
     * An empty list means "everyone in the group" for an equal split.
     */
    private List<SplitShare> parseShares(String spec, SplitType type) {
        List<SplitShare> shares = new ArrayList<>();
        if (spec.isBlank()) {
            if (type.needsValues()) {
                throw new ValidationException("A " + type.name().toLowerCase() + " split needs --with id:value,...");
            }
            ledger.members().forEach(m -> shares.add(SplitShare.of(m.id())));
            return shares;
        }
        for (String part : spec.split(",")) {
            String[] pieces = part.trim().split(":", 2);
            if (type.needsValues()) {
                if (pieces.length != 2) {
                    throw new ValidationException("Expected id:value but got '" + part + "'");
                }
                shares.add(new SplitShare(pieces[0], parseDecimal(pieces[1])));
            } else {
                shares.add(SplitShare.of(pieces[0]));
            }
        }
        return shares;
    }

    private static BigDecimal parseDecimal(String text) {
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Not a number: " + text);
        }
    }

    private static int parseId(String text) {
        try {
            return Integer.parseInt(text.replace("#", "").trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Not an expense id: " + text);
        }
    }

    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date '" + text + "', expected YYYY-MM-DD");
        }
    }

    private void listExpenses(ParsedCommand command) {
        LocalDate from = command.option("from").map(Cli::parseDate).orElse(null);
        LocalDate to = command.option("to").map(Cli::parseDate).orElse(null);
        Category category = command.option("category").map(Category::fromString).orElse(null);
        List<Expense> found = ledger.expensesBetween(from, to, category);
        if (command.option("member").isPresent()) {
            String id = ledger.member(command.option("member").get()).id();
            found = found.stream().filter(e -> e.involves(id)).toList();
        }
        if (found.isEmpty()) {
            out.println("No expenses found.");
            return;
        }
        found.forEach(out::println);
    }

    private void pay(ParsedCommand command) {
        Money amount = Money.parse(command.word(3));
        LocalDate date = command.option("date").map(Cli::parseDate).orElse(LocalDate.now(clock));
        Payment payment = ledger.recordPayment(command.word(1), command.word(2), amount, date);
        out.println("Recorded: " + payment);
    }

    private void balances() {
        ledger.balances().forEach((id, balance) -> {
            if (balance.isPositive()) {
                out.println(id + " is owed " + balance.format());
            } else if (balance.isNegative()) {
                out.println(id + " owes " + balance.abs().format());
            } else {
                out.println(id + " is settled up");
            }
        });
    }
}
