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
}
