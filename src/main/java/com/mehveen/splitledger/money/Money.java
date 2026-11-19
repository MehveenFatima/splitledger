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
}
