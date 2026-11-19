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
}
