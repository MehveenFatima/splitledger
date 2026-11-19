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
}
