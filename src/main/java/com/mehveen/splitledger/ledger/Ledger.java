package com.mehveen.splitledger.ledger;

import com.mehveen.splitledger.exception.NotFoundException;
import com.mehveen.splitledger.exception.ValidationException;
import com.mehveen.splitledger.model.Category;
import com.mehveen.splitledger.model.Expense;
import com.mehveen.splitledger.model.Member;
import com.mehveen.splitledger.model.Payment;
import com.mehveen.splitledger.money.Money;
import com.mehveen.splitledger.split.SplitShare;
import com.mehveen.splitledger.split.SplitStrategyFactory;
import com.mehveen.splitledger.split.SplitType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The core of SplitLedger: keeps members, expenses and payments for one group
 * and works out who owes whom.
 *
 * <p>Balance convention: a positive balance means the member <b>is owed</b>
 * money; a negative balance means the member <b>owes</b> money. The sum of all
 * balances is always zero.</p>
 */
public class Ledger {

    private final Map<String, Member> members = new LinkedHashMap<>();
    private final List<Expense> expenses = new ArrayList<>();
    private final List<Payment> payments = new ArrayList<>();
    private final SplitStrategyFactory strategies;
    private int nextExpenseId = 1;

    public Ledger() {
        this(new SplitStrategyFactory());
    }

    public Ledger(SplitStrategyFactory strategies) {
        this.strategies = Objects.requireNonNull(strategies, "strategies");
    }

    public Member addMember(Member member) {
        Objects.requireNonNull(member, "member");
        if (members.containsKey(member.id())) {
            throw new ValidationException("A member with id '" + member.id() + "' already exists");
        }
        members.put(member.id(), member);
        return member;
    }

    public Member member(String id) {
        Member member = id == null ? null : members.get(id.trim().toLowerCase(java.util.Locale.ROOT));
        if (member == null) {
            throw new NotFoundException("No member with id '" + id + "'");
        }
        return member;
    }

    public boolean hasMember(String id) {
        return id != null && members.containsKey(id.trim().toLowerCase(java.util.Locale.ROOT));
    }

    public List<Member> members() {
        return List.copyOf(members.values());
    }

    public Expense addExpense(String description, String payerId, Money amount, Category category,
                              SplitType splitType, List<SplitShare> shares, LocalDate date) {
        Member payer = member(payerId);
        if (shares != null) {
            for (SplitShare share : shares) {
                member(share.memberId());
            }
        }
        Map<String, Money> computed = strategies.forType(splitType).split(amount, shares);
        Expense expense = new Expense(nextExpenseId++, description, payer.id(), amount,
                category, splitType, computed, date);
        expenses.add(expense);
        return expense;
    }

    /** Adds an already computed expense, e.g. when loading from disk. */
    public Expense restoreExpense(Expense expense) {
        member(expense.payerId());
        expense.shares().keySet().forEach(this::member);
        if (findExpense(expense.id()).isPresent()) {
            throw new ValidationException("Duplicate expense id #" + expense.id());
        }
        expenses.add(expense);
        nextExpenseId = Math.max(nextExpenseId, expense.id() + 1);
        return expense;
    }

    public Optional<Expense> findExpense(int id) {
        return expenses.stream().filter(e -> e.id() == id).findFirst();
    }

    public Expense removeExpense(int id) {
        Expense expense = findExpense(id)
                .orElseThrow(() -> new NotFoundException("No expense with id #" + id));
        expenses.remove(expense);
        return expense;
    }

    public List<Expense> expenses() {
        return Collections.unmodifiableList(expenses);
    }

    public Payment recordPayment(String fromId, String toId, Money amount, LocalDate date) {
        Payment payment = new Payment(member(fromId).id(), member(toId).id(), amount, date);
        payments.add(payment);
        return payment;
    }

    public List<Payment> payments() {
        return Collections.unmodifiableList(payments);
    }

    /**
     * Net balance of every member. Payer is credited the full amount, every
     * participant is debited their share; a payment credits the payer and
     * debits the receiver.
     */
    public Map<String, Money> balances() {
        Map<String, Money> balances = new LinkedHashMap<>();
        members.keySet().forEach(id -> balances.put(id, Money.ZERO));
        for (Expense expense : expenses) {
            balances.merge(expense.payerId(), expense.amount(), Money::plus);
            expense.shares().forEach((id, share) -> balances.merge(id, share.negate(), Money::plus));
        }
        for (Payment payment : payments) {
            balances.merge(payment.fromId(), payment.amount(), Money::plus);
            balances.merge(payment.toId(), payment.amount().negate(), Money::plus);
        }
        return Collections.unmodifiableMap(balances);
    }

    public Money balanceOf(String memberId) {
        return balances().get(member(memberId).id());
    }

    public boolean isSettled() {
        return balances().values().stream().allMatch(Money::isZero);
    }
}
