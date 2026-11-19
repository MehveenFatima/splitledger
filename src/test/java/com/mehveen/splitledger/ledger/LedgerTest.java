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

    @Test
    void settledAfterAllPayments() {
        dinner("riya", "900");
        ledger.recordPayment("aman", "riya", Money.ofRupees(300), NOV_19);
        assertFalse(ledger.isSettled());
        ledger.recordPayment("sam", "riya", Money.ofRupees(300), NOV_19);
        assertTrue(ledger.isSettled());
    }

    @Test
    void rejectsPaymentWithUnknownMember() {
        assertThrows(NotFoundException.class, () -> ledger.recordPayment("zoe", "riya", Money.ofRupees(1), NOV_19));
        assertTrue(ledger.payments().isEmpty());
    }

    @Test
    void totalSpentAddsExpenses() {
        dinner("riya", "100.50");
        dinner("sam", "99.50");
        assertEquals(Money.ofRupees(200), ledger.totalSpent());
    }

    @Test
    void totalsByCategory() {
        dinner("riya", "300");
        ledger.addExpense("Cab", "aman", Money.ofRupees(120), Category.TRAVEL, SplitType.EQUAL,
                List.of(SplitShare.of("aman"), SplitShare.of("sam")), NOV_19);
        Map<Category, Money> totals = ledger.totalsByCategory();
        assertEquals(Money.ofRupees(300), totals.get(Category.FOOD));
        assertEquals(Money.ofRupees(120), totals.get(Category.TRAVEL));
        assertFalse(totals.containsKey(Category.RENT));
    }

    @Test
    void spendingOfSumsShares() {
        dinner("riya", "300");
        ledger.addExpense("Cab", "aman", Money.ofRupees(120), Category.TRAVEL, SplitType.EQUAL,
                List.of(SplitShare.of("aman"), SplitShare.of("sam")), NOV_19);
        assertEquals(Money.ofRupees(100), ledger.spendingOf("riya"));
        assertEquals(Money.ofRupees(160), ledger.spendingOf("sam"));
    }

    @Test
    void expensesInvolvingMember() {
        ledger.addExpense("Gift", "riya", Money.ofRupees(50), Category.SHOPPING, SplitType.EQUAL,
                List.of(SplitShare.of("aman")), NOV_19);
        ledger.addExpense("Cab", "aman", Money.ofRupees(60), Category.TRAVEL, SplitType.EQUAL,
                List.of(SplitShare.of("aman"), SplitShare.of("sam")), NOV_19);
        assertEquals(1, ledger.expensesInvolving("riya").size());
        assertEquals(2, ledger.expensesInvolving("aman").size());
    }

    @Test
    void filtersByDateRange() {
        ledger.addExpense("Rent", "sam", Money.ofRupees(300), Category.RENT, SplitType.EQUAL,
                List.of(SplitShare.of("sam"), SplitShare.of("riya")), NOV_1);
        dinner("riya", "300");
        assertEquals(1, ledger.expensesBetween(LocalDate.of(2026, 11, 10), null, null).size());
        assertEquals(1, ledger.expensesBetween(null, LocalDate.of(2026, 11, 10), null).size());
        assertEquals(2, ledger.expensesBetween(NOV_1, NOV_19, null).size());
    }

    @Test
    void filtersByCategory() {
        dinner("riya", "300");
        assertEquals(1, ledger.expensesBetween(null, null, Category.FOOD).size());
        assertTrue(ledger.expensesBetween(null, null, Category.RENT).isEmpty());
    }

    @Test
    void removeExpenseUpdatesBalances() {
        Expense expense = dinner("riya", "900");
        ledger.removeExpense(expense.id());
        assertTrue(ledger.isSettled());
        assertThrows(NotFoundException.class, () -> ledger.removeExpense(expense.id()));
    }

    @Test
    void restoreKeepsIdsAndContinuesNumbering() {
        Expense saved = new Expense(7, "Old trip", "riya", Money.ofRupees(10), Category.TRAVEL, SplitType.EQUAL,
                Map.of("riya", Money.ofRupees(10)), NOV_1);
        ledger.restoreExpense(saved);
        assertEquals(8, dinner("aman", "30").id());
    }
}
