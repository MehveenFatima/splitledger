package com.mehveen.splitledger.io;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvCodecTest {

    @Test
    void encodesPlainFields() {
        assertEquals("MEMBER,riya,Riya", CsvCodec.encode(List.of("MEMBER", "riya", "Riya")));
    }

    @Test
    void quotesFieldsWithCommas() {
        assertEquals("\"Dinner, drinks\"", CsvCodec.escape("Dinner, drinks"));
    }

    @Test
    void doublesEmbeddedQuotes() {
        assertEquals("\"The \"\"best\"\" pizza\"", CsvCodec.escape("The \"best\" pizza"));
    }

    @Test
    void quotesSurroundingWhitespace() {
        assertEquals("\" padded \"", CsvCodec.escape(" padded "));
    }

    @Test
    void nullBecomesEmpty() {
        assertEquals("a,,b", CsvCodec.encode(Arrays.asList("a", null, "b")));
    }

    @Test
    void rejectsLineBreaks() {
        assertThrows(IllegalArgumentException.class, () -> CsvCodec.escape("two\nlines"));
    }

    @Test
    void decodesSimpleAndEmptyFields() {
        assertEquals(List.of("a", "", "c"), CsvCodec.decode("a,,c"));
        assertEquals(List.of(""), CsvCodec.decode(""));
    }

    @Test
    void decodesQuotedFields() {
        assertEquals(List.of("x", "Dinner, \"fancy\""), CsvCodec.decode("x,\"Dinner, \"\"fancy\"\"\""));
    }

    @Test
    void rejectsUnterminatedQuote() {
        assertThrows(IllegalArgumentException.class, () -> CsvCodec.decode("a,\"oops"));
    }

    @Test
    void roundTrip() {
        List<String> fields = List.of("EXPENSE", "Chai, samosa", "He said \"wow\"", " x ", "");
        assertEquals(fields, CsvCodec.decode(CsvCodec.encode(fields)));
    }
}
