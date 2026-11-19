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

    @Test
    void keepsQuotedTextTogether() {
        assertEquals(List.of("expense", "add", "Dinner at Paradise", "1200"),
                CommandParser.tokenize("expense add \"Dinner at Paradise\" 1200"));
    }

    @Test
    void emptyQuotesGiveEmptyWord() {
        assertEquals(List.of("a", "", "b"), CommandParser.tokenize("a \"\" b"));
    }
}
