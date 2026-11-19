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

    public static Money min(Money a, Money b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    /**
     * Splits this amount into parts proportional to the given weights.
     * Every paisa is allocated: leftover paise go to the parts with the
     * largest fractional remainder (earlier parts win ties), so the parts
     * always add up exactly to this amount.
     */
    public List<Money> allocate(List<BigDecimal> weights) {
        if (paise < 0) {
            throw new IllegalArgumentException("Cannot allocate a negative amount");
        }
        if (weights == null || weights.isEmpty()) {
            throw new IllegalArgumentException("At least one weight is required");
        }
        BigDecimal totalWeight = BigDecimal.ZERO;
        for (BigDecimal weight : weights) {
            if (weight == null || weight.signum() < 0) {
                throw new IllegalArgumentException("Weights must be zero or positive");
            }
            totalWeight = totalWeight.add(weight);
        }
        if (totalWeight.signum() == 0) {
            throw new IllegalArgumentException("Total weight must be greater than zero");
        }

        int n = weights.size();
        long[] parts = new long[n];
        BigDecimal[] remainders = new BigDecimal[n];
        long allocated = 0;
        BigDecimal amount = BigDecimal.valueOf(paise);
        for (int i = 0; i < n; i++) {
            BigDecimal exact = amount.multiply(weights.get(i)).divide(totalWeight, 10, RoundingMode.DOWN);
            long floor = exact.setScale(0, RoundingMode.DOWN).longValueExact();
            parts[i] = floor;
            remainders[i] = exact.subtract(BigDecimal.valueOf(floor));
            allocated += floor;
        }

        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            order.add(i);
        }
        order.sort(Comparator.<Integer, BigDecimal>comparing(i -> remainders[i]).reversed()
                .thenComparing(i -> i));
        long leftover = paise - allocated;
        for (int k = 0; k < leftover; k++) {
            parts[order.get(k % n)]++;
        }

        List<Money> result = new ArrayList<>(n);
        for (long part : parts) {
            result.add(ofPaise(part));
        }
        return Collections.unmodifiableList(result);
    }
}
