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
}
