package com.mehveen.splitledger.io;

import com.mehveen.splitledger.exception.LedgerException;
import com.mehveen.splitledger.ledger.Ledger;
import com.mehveen.splitledger.model.Category;
import com.mehveen.splitledger.model.Member;
import com.mehveen.splitledger.money.Money;
import com.mehveen.splitledger.split.SplitShare;
import com.mehveen.splitledger.split.SplitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LedgerStoreTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 19);

    private final LedgerStore store = new LedgerStore();
    private Path dir;

    @BeforeEach
    void setUp() throws IOException {
        dir = Files.createTempDirectory("splitledger-test");
    }

    private static Ledger sampleLedger() {
        Ledger ledger = new Ledger();
        ledger.addMember(new Member("riya", "Riya Sharma"));
        ledger.addMember(new Member("sam", "Sam, Jr."));
        ledger.addExpense("Dinner, \"special\"", "riya", Money.parse("1000.01"), Category.FOOD, SplitType.EQUAL,
                List.of(SplitShare.of("riya"), SplitShare.of("sam")), DAY);
        ledger.addExpense("Rent", "sam", Money.ofRupees(900), Category.RENT, SplitType.SHARES,
                List.of(SplitShare.of("riya", "2"), SplitShare.of("sam", "1")), DAY);
        ledger.recordPayment("sam", "riya", Money.ofRupees(100), DAY);
        return ledger;
    }

    @Test
    void roundTripKeepsEverything() {
        Ledger original = sampleLedger();
        Path file = dir.resolve("trip.csv");
        store.save(original, file);
        Ledger loaded = store.load(file);

        assertEquals(original.members(), loaded.members());
        assertEquals(original.expenses().size(), loaded.expenses().size());
        assertEquals(original.payments(), loaded.payments());
        assertEquals(original.balances(), loaded.balances());
        assertEquals("Dinner, \"special\"", loaded.expenses().get(0).description());
    }

    @Test
    void keepsExpenseIdsAndSplitType() {
        Path file = dir.resolve("trip.csv");
        store.save(sampleLedger(), file);
        Ledger loaded = store.load(file);
        assertEquals(2, loaded.expenses().get(1).id());
        assertEquals(SplitType.SHARES, loaded.expenses().get(1).splitType());
        assertEquals(Money.ofRupees(600), loaded.expenses().get(1).shareOf("riya"));
    }

    @Test
    void writesVersionHeader() throws IOException {
        Path file = dir.resolve("trip.csv");
        store.save(sampleLedger(), file);
        assertEquals(LedgerStore.HEADER, Files.readAllLines(file).get(0));
    }
}
