package com.mehveen.splitledger.split;

import com.mehveen.splitledger.exception.ValidationException;

import java.util.Locale;

/** The ways an expense can be divided between participants. */
public enum SplitType {
    /** Everyone pays the same amount. */
    EQUAL,
    /** Each participant pays a fixed amount. */
    EXACT,
    /** Each participant pays a percentage; percentages add up to 100. */
    PERCENT,
    /** Each participant pays in proportion to a number of shares (e.g. 2:1:1). */
    SHARES;

    public static SplitType fromString(String text) {
        if (text == null || text.isBlank()) {
            return EQUAL;
        }
        try {
            return valueOf(text.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidationException(
                    "Unknown split type '" + text + "'. Use equal, exact, percent or shares");
        }
    }
}
