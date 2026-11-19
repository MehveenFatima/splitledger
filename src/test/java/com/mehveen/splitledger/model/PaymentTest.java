package com.mehveen.splitledger.model;

import com.mehveen.splitledger.exception.ValidationException;
import com.mehveen.splitledger.money.Money;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 19);

    @Test
    void rejectsSelfPayment() {
        assertThrows(ValidationException.class, () -> new Payment("riya", "riya", Money.ofRupees(1), DAY));
    }

    @Test
    void rejectsZeroOrNegativeAmount() {
        assertThrows(ValidationException.class, () -> new Payment("riya", "sam", Money.ZERO, DAY));
        assertThrows(ValidationException.class, () -> new Payment("riya", "sam", Money.ofRupees(-1), DAY));
    }
}
