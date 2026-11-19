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

    @Test
    void rejectsMissingClosingQuote() {
        assertThrows(ValidationException.class, () -> CommandParser.tokenize("expense add \"Dinner"));
    }

    @Test
    void separatesOptions() {
        ParsedCommand command = CommandParser.parse("expense add Cab 300 --payer riya --with riya,sam");
        assertEquals(List.of("expense", "add", "Cab", "300"), command.words());
        assertEquals("riya", command.requireOption("payer"));
        assertEquals("riya,sam", command.option("with").orElseThrow());
    }

    @Test
    void optionNamesAreLowercased() {
        assertEquals("food", CommandParser.parse("expenses --CATEGORY food").requireOption("category"));
    }

    @Test
    void optionNeedsValue() {
        assertThrows(ValidationException.class, () -> CommandParser.parse("expense add --payer"));
        assertThrows(ValidationException.class, () -> CommandParser.parse("expense add --payer --with a"));
    }

    @Test
    void blankInputIsEmpty() {
        assertTrue(CommandParser.parse("   ").isEmpty());
        assertTrue(CommandParser.parse(null).isEmpty());
        assertFalse(CommandParser.parse("help").isEmpty());
    }
}
