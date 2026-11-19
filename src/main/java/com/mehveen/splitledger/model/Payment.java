package com.mehveen.splitledger.model;

import com.mehveen.splitledger.exception.ValidationException;
import com.mehveen.splitledger.money.Money;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Money handed from one member to another to settle a debt.
 *
 * @param fromId member who paid
 * @param toId   member who received
 */
public record Payment(String fromId, String toId, Money amount, LocalDate date) {

    public Payment {
        Objects.requireNonNull(fromId, "fromId");
        Objects.requireNonNull(toId, "toId");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(date, "date");
        if (fromId.equals(toId)) {
            throw new ValidationException("A member cannot pay themselves");
        }
        if (!amount.isPositive()) {
            throw new ValidationException("Payment amount must be greater than zero");
        }
    }

    @Override
    public String toString() {
        return date + " " + fromId + " paid " + toId + " " + amount.format();
    }
}
