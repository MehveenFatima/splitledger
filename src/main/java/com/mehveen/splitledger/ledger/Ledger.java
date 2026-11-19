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
}
