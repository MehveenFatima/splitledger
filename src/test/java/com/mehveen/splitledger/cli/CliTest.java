package com.mehveen.splitledger.cli;

import com.mehveen.splitledger.ledger.Ledger;
import com.mehveen.splitledger.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliTest {

    private ByteArrayOutputStream buffer;
    private Cli cli;

    @BeforeEach
    void setUp() {
        buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        Clock clock = Clock.fixed(Instant.parse("2026-11-19T10:00:00Z"), ZoneOffset.UTC);
        cli = new Cli(new Ledger(), out, clock);
    }

    /** Runs commands and returns only the output of the last one. */
    private String run(String... lines) {
        for (String line : lines) {
            buffer.reset();
            cli.execute(line);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private void addGroup() {
        run("member add \"Riya Sharma\"", "member add Aman", "member add Sam");
    }

    @Test
    void helpListsCommands() {
        String out = run("help");
        assertTrue(out.contains("expense add"));
        assertTrue(out.contains("settle"));
    }

    @Test
    void memberAddDerivesId() {
        assertTrue(run("member add \"Riya Sharma\"").contains("Added Riya Sharma (id: riya)"));
    }

    @Test
    void memberAddWithCustomId() {
        run("member add \"Riya Sharma\" --id rs");
        assertTrue(cli.ledger().hasMember("rs"));
    }

    @Test
    void membersWhenEmpty() {
        assertTrue(run("members").contains("No members yet"));
    }

    @Test
    void equalExpenseDefaultsToEveryone() {
        addGroup();
        run("expense add Dinner 900 --payer riya --category food");
        assertEquals(Money.ofRupees(600), cli.ledger().balanceOf("riya"));
        assertEquals(Money.ofRupees(-300), cli.ledger().balanceOf("sam"));
    }

    @Test
    void expenseDateDefaultsToClock() {
        addGroup();
        run("expense add Dinner 900 --payer riya");
        assertEquals(LocalDate.of(2026, 11, 19), cli.ledger().expenses().get(0).date());
    }

    @Test
    void sharesSplitFromOptions() {
        addGroup();
        run("expense add Rent 30000 --payer sam --split shares --with riya:2,aman:1,sam:1 --category rent");
        assertEquals(Money.ofRupees(15_000), cli.ledger().spendingOf("riya"));
    }

    @Test
    void nonEqualSplitNeedsWith() {
        addGroup();
        String out = run("expense add Rent 300 --payer sam --split exact");
        assertTrue(out.startsWith("Error:"));
        assertTrue(cli.ledger().expenses().isEmpty());
    }

    @Test
    void malformedPairIsAnError() {
        addGroup();
        assertTrue(run("expense add Rent 300 --payer sam --split exact --with riya").startsWith("Error:"));
        assertTrue(run("expense add Rent 300 --payer sam --split exact --with riya:abc").contains("Not a number"));
    }

    @Test
    void expenseRemove() {
        addGroup();
        run("expense add Dinner 900 --payer riya");
        assertTrue(run("expense remove #1").contains("Removed expense #1"));
        assertTrue(cli.ledger().expenses().isEmpty());
    }

    @Test
    void balancesInPlainWords() {
        addGroup();
        run("expense add Dinner 900 --payer riya");
        String out = run("balances");
        assertTrue(out.contains("riya is owed Rs 600.00"));
        assertTrue(out.contains("aman owes Rs 300.00"));
    }

    @Test
    void settleShowsPayments() {
        addGroup();
        run("expense add Dinner 900 --payer riya");
        String out = run("settle");
        assertTrue(out.contains("2 payment(s)"));
        assertTrue(out.contains("aman pays riya Rs 300.00"));
    }

    @Test
    void paySettlesMember() {
        addGroup();
        run("expense add Dinner 900 --payer riya");
        run("pay aman riya 300");
        assertTrue(run("balances").contains("aman is settled up"));
    }

    @Test
    void settledGroupMessage() {
        addGroup();
        assertTrue(run("settle").contains("Everyone is settled up."));
    }

    @Test
    void expensesFilteredByMember() {
        addGroup();
        run("expense add Cab 100 --payer aman --with aman,sam");
        run("expense add Tea 30 --payer riya --with riya");
        String out = run("expenses --member sam");
        assertTrue(out.contains("Cab"));
        assertFalse(out.contains("Tea"));
    }

    @Test
    void statsShowsTopSpender() {
        addGroup();
        run("expense add Rent 30000 --payer sam --split shares --with riya:2,aman:1,sam:1");
        String out = run("stats");
        assertTrue(out.contains("Total spent: Rs 30000.00"));
        assertTrue(out.contains("Top spender: riya"));
    }

    @Test
    void saveAndLoad() throws IOException {
        addGroup();
        run("expense add Dinner 900 --payer riya");
        Path file = Files.createTempDirectory("cli-test").resolve("trip.csv");
        run("save " + file);
        run("member add Zoe");
        assertTrue(run("load " + file).contains("Loaded 3 members and 1 expenses"));
        assertFalse(cli.ledger().hasMember("zoe"));
    }

    @Test
    void exportWritesMarkdownFile() throws IOException {
        addGroup();
        run("expense add Dinner 900 --payer riya");
        Path file = Files.createTempDirectory("cli-export").resolve("summary.md");
        assertTrue(run("export " + file + " --title \"Goa trip\"").contains("Exported Markdown summary"));
        String md = Files.readString(file);
        assertTrue(md.startsWith("# Goa trip"));
        assertTrue(md.contains("- [ ] aman pays riya Rs 300.00"));
    }

    @Test
    void expensesSortedByAmountWithLimit() {
        addGroup();
        run("expense add Tea 30 --payer riya");
        run("expense add Rent 3000 --payer sam");
        run("expense add Cab 300 --payer aman");
        String out = run("expenses --sort amount --limit 2");
        assertTrue(out.indexOf("Rent") < out.indexOf("Cab"));
        assertFalse(out.contains("Tea"));
    }

    @Test
    void expensesSortedByDate() {
        addGroup();
        run("expense add Late 10 --payer riya --date 2026-11-20");
        run("expense add Early 10 --payer riya --date 2026-11-01");
        String out = run("expenses --sort date");
        assertTrue(out.indexOf("Early") < out.indexOf("Late"));
    }

    @Test
    void invalidSortOrLimitIsAnError() {
        addGroup();
        run("expense add Tea 30 --payer riya");
        assertTrue(run("expenses --sort name").startsWith("Error:"));
        assertTrue(run("expenses --limit 0").startsWith("Error:"));
        assertTrue(run("expenses --limit ten").startsWith("Error:"));
    }

    @Test
    void unknownCommandPointsToHelp() {
        assertTrue(run("dance").contains("Type 'help'"));
    }

    @Test
    void badInputPrintsError() {
        addGroup();
        assertTrue(run("expense add Dinner lots --payer riya").startsWith("Error:"));
        assertTrue(run("pay riya ghost 10").startsWith("Error:"));
        assertTrue(run("load /definitely/not/here.csv").startsWith("Error:"));
    }

    @Test
    void exitStopsLoop() {
        assertFalse(cli.execute("exit"));
        assertFalse(cli.execute("QUIT"));
        assertTrue(cli.execute(""));
    }
}
