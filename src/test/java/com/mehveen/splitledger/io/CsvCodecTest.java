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
}
