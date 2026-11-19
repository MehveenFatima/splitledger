package com.mehveen.splitledger.cli;

import com.mehveen.splitledger.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandParserTest {

    @Test
    void splitsOnWhitespace() {
        assertEquals(List.of("member", "add", "Riya"), CommandParser.tokenize("  member   add\tRiya "));
    }
}
