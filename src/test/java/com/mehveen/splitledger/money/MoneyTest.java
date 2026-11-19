package com.mehveen.splitledger.money;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyTest {

    @Test
    void ofPaiseZeroReturnsSharedZero() {
        assertSame(Money.ZERO, Money.ofPaise(0));
    }

    @Test
    void ofRupeesConvertsToPaise() {
        assertEquals(12_500L, Money.ofRupees(125).paise());
    }

    @Test
    void ofRupeesRejectsOverflow() {
        assertThrows(ArithmeticException.class, () -> Money.ofRupees(Long.MAX_VALUE));
    }

    @Test
    void parsesWholeNumbers() {
        assertEquals(120_000L, Money.parse("1200").paise());
    }

    @Test
    void parsesDecimals() {
        assertEquals(1050L, Money.parse("10.5").paise());
        assertEquals(1099L, Money.parse("10.99").paise());
    }

    @Test
    void parseAcceptsTrailingZeros() {
        assertEquals(150L, Money.parse("1.500").paise());
    }

    @Test
    void parseRejectsFractionsOfPaise() {
        assertThrows(IllegalArgumentException.class, () -> Money.parse("1.005"));
    }

    @Test
    void parseRejectsGarbageAndBlank() {
        assertThrows(IllegalArgumentException.class, () -> Money.parse("ten"));
        assertThrows(IllegalArgumentException.class, () -> Money.parse("  "));
        assertThrows(IllegalArgumentException.class, () -> Money.parse(null));
    }

    @Test
    void parseTrimsWhitespace() {
        assertEquals(Money.ofRupees(5), Money.parse("  5.00 "));
    }

    @Test
    void plusAndMinus() {
        Money a = Money.parse("10.25");
        Money b = Money.parse("2.75");
        assertEquals(Money.parse("13.00"), a.plus(b));
        assertEquals(Money.parse("7.50"), a.minus(b));
    }
}
