package com.mehveen.splitledger.model;

import com.mehveen.splitledger.exception.ValidationException;
import com.mehveen.splitledger.money.Money;
import com.mehveen.splitledger.split.SplitType;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An expense paid by one member and shared by one or more members.
 * Immutable once created; the shares are already computed by a split strategy.
 */
public final class Expense {

    private final int id;
    private final String description;
    private final String payerId;
    private final Money amount;
    private final Category category;
    private final SplitType splitType;
    private final Map<String, Money> shares;
    private final LocalDate date;

    public Expense(int id, String description, String payerId, Money amount, Category category,
                   SplitType splitType, Map<String, Money> shares, LocalDate date) {
        if (id <= 0) {
            throw new ValidationException("Expense id must be positive");
        }
        if (description == null || description.isBlank()) {
            throw new ValidationException("Expense description is required");
        }
        this.id = id;
        this.description = description.trim();
        this.payerId = Objects.requireNonNull(payerId, "payerId");
        this.amount = Objects.requireNonNull(amount, "amount");
        this.category = category == null ? Category.OTHER : category;
        this.splitType = splitType == null ? SplitType.EQUAL : splitType;
        this.shares = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(shares, "shares")));
        this.date = Objects.requireNonNull(date, "date");
        validateShares();
    }

    private void validateShares() {
        if (shares.isEmpty()) {
            throw new ValidationException("An expense needs at least one participant");
        }
        Money sum = shares.values().stream().reduce(Money.ZERO, Money::plus);
        if (!sum.equals(amount)) {
            throw new ValidationException("Shares add up to " + sum.format() + " but amount is " + amount.format());
        }
    }

    public int id() {
        return id;
    }

    public String description() {
        return description;
    }

    public String payerId() {
        return payerId;
    }

    public Money amount() {
        return amount;
    }

    public Category category() {
        return category;
    }

    public SplitType splitType() {
        return splitType;
    }

    public Map<String, Money> shares() {
        return shares;
    }

    public LocalDate date() {
        return date;
    }
}
