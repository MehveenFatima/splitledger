package com.mehveen.splitledger.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Immutable amount of money stored as whole paise (1/100 of a rupee).
 *
 * <p>Using a {@code long} instead of {@code double} avoids floating point
 * rounding errors such as {@code 0.1 + 0.2 != 0.3}.</p>
 */
public final class Money implements Comparable<Money> {

    public static final Money ZERO = new Money(0);

    private final long paise;

    private Money(long paise) {
        this.paise = paise;
    }

    public static Money ofPaise(long paise) {
        return paise == 0 ? ZERO : new Money(paise);
    }

    public long paise() {
        return paise;
    }

    public static Money ofRupees(long rupees) {
        return ofPaise(Math.multiplyExact(rupees, 100L));
    }

    public static Money parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Amount is required");
        }
        BigDecimal value;
        try {
            value = new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount: " + text);
        }
        if (value.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Amount can have at most 2 decimal places: " + text);
        }
        return ofPaise(value.movePointRight(2).longValueExact());
    }

    public Money plus(Money other) {
        return ofPaise(Math.addExact(paise, other.paise));
    }

    public Money minus(Money other) {
        return ofPaise(Math.subtractExact(paise, other.paise));
    }

    public Money negate() {
        return ofPaise(Math.negateExact(paise));
    }

    public Money abs() {
        return paise < 0 ? negate() : this;
    }

    public boolean isZero() {
        return paise == 0;
    }

    public boolean isPositive() {
        return paise > 0;
    }

    public boolean isNegative() {
        return paise < 0;
    }
}
