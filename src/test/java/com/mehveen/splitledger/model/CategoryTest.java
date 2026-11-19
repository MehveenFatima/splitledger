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

    @Test
    void blankDefaultsToOther() {
        assertEquals(Category.OTHER, Category.fromString(""));
        assertEquals(Category.OTHER, Category.fromString(null));
    }

    @Test
    void unknownCategoryListsOptions() {
        ValidationException e = assertThrows(ValidationException.class, () -> Category.fromString("pets"));
        assertTrue(e.getMessage().contains("food"));
        assertTrue(e.getMessage().contains("rent"));
    }

    @Test
    void labelIsLowercase() {
        assertEquals("entertainment", Category.ENTERTAINMENT.label());
    }
}
