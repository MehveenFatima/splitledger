package com.mehveen.splitledger.split;

import com.mehveen.splitledger.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SplitTypeTest {

    @Test
    void parsesIgnoringCase() {
        assertEquals(SplitType.PERCENT, SplitType.fromString("Percent"));
        assertEquals(SplitType.SHARES, SplitType.fromString("shares"));
    }
}
