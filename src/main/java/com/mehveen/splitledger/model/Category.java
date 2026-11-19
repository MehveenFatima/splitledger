package com.mehveen.splitledger.model;

import com.mehveen.splitledger.exception.ValidationException;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/** Spending category used for reports. */
public enum Category {
    FOOD,
    TRAVEL,
    RENT,
    UTILITIES,
    SHOPPING,
    ENTERTAINMENT,
    OTHER;

    public static Category fromString(String text) {
        if (text == null || text.isBlank()) {
            return OTHER;
        }
        try {
            return valueOf(text.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            String options = Arrays.stream(values())
                    .map(c -> c.name().toLowerCase(Locale.ROOT))
                    .collect(Collectors.joining(", "));
            throw new ValidationException("Unknown category '" + text + "'. Use one of: " + options);
        }
    }

    public String label() {
        return name().toLowerCase(Locale.ROOT);
    }
}
