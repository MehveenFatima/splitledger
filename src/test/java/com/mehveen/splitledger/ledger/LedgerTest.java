package com.mehveen.splitledger.ledger;

import com.mehveen.splitledger.exception.NotFoundException;
import com.mehveen.splitledger.exception.ValidationException;
import com.mehveen.splitledger.model.Category;
import com.mehveen.splitledger.model.Expense;
import com.mehveen.splitledger.model.Member;
import com.mehveen.splitledger.money.Money;
import com.mehveen.splitledger.split.SplitShare;
import com.mehveen.splitledger.split.SplitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LedgerTest {

    private static final LocalDate NOV_1 = LocalDate.of(2026, 11, 1);
    private static final LocalDate NOV_19 = LocalDate.of(2026, 11, 19);

    private Ledger ledger;

    @BeforeEach
    void setUp() {
        ledger = new Ledger();
        ledger.addMember(new Member("riya", "Riya"));
        ledger.addMember(new Member("aman", "Aman"));
        ledger.addMember(new Member("sam", "Sam"));
    }

    private Expense dinner(String payer, String amount) {
        return ledger.addExpense("Dinner", payer, Money.parse(amount), Category.FOOD, SplitType.EQUAL,
                List.of(SplitShare.of("riya"), SplitShare.of("aman"), SplitShare.of("sam")), NOV_19);
    }

    @Test
    void startsWithMembersAndNoExpenses() {
        assertEquals(3, ledger.members().size());
        assertTrue(ledger.expenses().isEmpty());
        assertTrue(ledger.isSettled());
    }

    @Test
    void rejectsDuplicateMember() {
        assertThrows(ValidationException.class, () -> ledger.addMember(new Member("RIYA", "Another Riya")));
    }

    @Test
    void memberLookupIgnoresCase() {
        assertEquals("Aman", ledger.member(" AMAN ").name());
        assertTrue(ledger.hasMember("Sam"));
        assertFalse(ledger.hasMember("zoe"));
    }

    @Test
    void unknownMemberIsNotFound() {
        assertThrows(NotFoundException.class, () -> ledger.member("zoe"));
        assertThrows(NotFoundException.class, () -> ledger.member(null));
    }

    @Test
    void assignsIncreasingExpenseIds() {
        assertEquals(1, dinner("riya", "300").id());
        assertEquals(2, dinner("aman", "300").id());
    }

    @Test
    void rejectsUnknownPayerOrParticipant() {
        assertThrows(NotFoundException.class, () -> dinner("zoe", "300"));
        assertThrows(NotFoundException.class, () -> ledger.addExpense("Cab", "riya", Money.ofRupees(10),
                Category.TRAVEL, SplitType.EQUAL, List.of(SplitShare.of("zoe")), NOV_19));
        assertTrue(ledger.expenses().isEmpty());
    }

    @Test
    void balancesAfterOneExpense() {
        dinner("riya", "900");
        Map<String, Money> balances = ledger.balances();
        assertEquals(Money.ofRupees(600), balances.get("riya"));
        assertEquals(Money.ofRupees(-300), balances.get("aman"));
        assertEquals(Money.ofRupees(-300), balances.get("sam"));
    }

    @Test
    void balancesSumToZero() {
        dinner("riya", "1000");
        dinner("aman", "333.33");
        ledger.addExpense("Rent", "sam", Money.ofRupees(30000), Category.RENT, SplitType.SHARES,
                List.of(SplitShare.of("riya", "2"), SplitShare.of("aman", "1"), SplitShare.of("sam", "1")), NOV_1);
        Money sum = ledger.balances().values().stream().reduce(Money.ZERO, Money::plus);
        assertEquals(Money.ZERO, sum);
    }

    @Test
    void paymentsMoveBalances() {
        dinner("riya", "900");
        ledger.recordPayment("aman", "riya", Money.ofRupees(300), NOV_19);
        assertEquals(Money.ZERO, ledger.balanceOf("aman"));
        assertEquals(Money.ofRupees(300), ledger.balanceOf("riya"));
    }
}
