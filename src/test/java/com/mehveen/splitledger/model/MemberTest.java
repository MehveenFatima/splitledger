package com.mehveen.splitledger.model;

import com.mehveen.splitledger.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MemberTest {

    @Test
    void normalizesIdAndName() {
        Member member = new Member("  RIYA ", "  Riya Sharma ");
        assertEquals("riya", member.id());
        assertEquals("Riya Sharma", member.name());
    }

    @Test
    void rejectsMissingIdOrName() {
        assertThrows(ValidationException.class, () -> new Member(" ", "Riya"));
        assertThrows(ValidationException.class, () -> new Member(null, "Riya"));
        assertThrows(ValidationException.class, () -> new Member("riya", " "));
    }

    @Test
    void rejectsInvalidIdCharacters() {
        assertThrows(ValidationException.class, () -> new Member("riya sharma", "Riya"));
        assertThrows(ValidationException.class, () -> new Member("riya@home", "Riya"));
    }

    @Test
    void rejectsTooLongId() {
        assertThrows(ValidationException.class, () -> new Member("a".repeat(21), "Long"));
    }
}
