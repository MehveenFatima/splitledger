package com.mehveen.splitledger.model;

import com.mehveen.splitledger.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryTest {

    @Test
    void parsesIgnoringCase() {
        assertEquals(Category.FOOD, Category.fromString("Food"));
        assertEquals(Category.TRAVEL, Category.fromString(" travel "));
    }
}
