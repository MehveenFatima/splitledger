package com.mehveen.splitledger.model;

import com.mehveen.splitledger.exception.ValidationException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * A person taking part in the ledger.
 *
 * @param id   short unique handle such as {@code riya}; lowercase letters, digits, '-' or '_'
 * @param name display name such as {@code Riya Sharma}
 */
public record Member(String id, String name) {

    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_-]{1,20}");

    public Member {
        if (id == null || id.isBlank()) {
            throw new ValidationException("Member id is required");
        }
        id = id.trim().toLowerCase(Locale.ROOT);
        if (!ID_PATTERN.matcher(id).matches()) {
            throw new ValidationException(
                    "Member id must be 1-20 characters of a-z, 0-9, '-' or '_': " + id);
        }
        if (name == null || name.isBlank()) {
            throw new ValidationException("Member name is required");
        }
        name = name.trim();
    }

    /** Creates a member whose id is derived from the first word of the name. */
    public static Member of(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Member name is required");
        }
        String firstWord = name.trim().split("\\s+")[0];
        String id = firstWord.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "");
        if (id.length() > 20) {
            id = id.substring(0, 20);
        }
        return new Member(id, name);
    }
}
