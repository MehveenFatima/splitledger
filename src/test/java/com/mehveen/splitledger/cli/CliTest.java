package com.mehveen.splitledger.cli;

import com.mehveen.splitledger.ledger.Ledger;
import com.mehveen.splitledger.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliTest {

    private ByteArrayOutputStream buffer;
    private Cli cli;

    @BeforeEach
    void setUp() {
        buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        Clock clock = Clock.fixed(Instant.parse("2026-11-19T10:00:00Z"), ZoneOffset.UTC);
        cli = new Cli(new Ledger(), out, clock);
    }

    /** Runs commands and returns only the output of the last one. */
    private String run(String... lines) {
        for (String line : lines) {
            buffer.reset();
            cli.execute(line);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private void addGroup() {
        run("member add \"Riya Sharma\"", "member add Aman", "member add Sam");
    }

    @Test
    void helpListsCommands() {
        String out = run("help");
        assertTrue(out.contains("expense add"));
        assertTrue(out.contains("settle"));
    }

    @Test
    void memberAddDerivesId() {
        assertTrue(run("member add \"Riya Sharma\"").contains("Added Riya Sharma (id: riya)"));
    }

    @Test
    void memberAddWithCustomId() {
        run("member add \"Riya Sharma\" --id rs");
        assertTrue(cli.ledger().hasMember("rs"));
    }

    @Test
    void membersWhenEmpty() {
        assertTrue(run("members").contains("No members yet"));
    }
}
