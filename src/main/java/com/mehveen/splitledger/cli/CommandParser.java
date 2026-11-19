package com.mehveen.splitledger.cli;

import com.mehveen.splitledger.exception.ValidationException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Turns a raw input line into a {@link ParsedCommand}. Supports "double quoted" words. */
public final class CommandParser {

    private CommandParser() {
    }

    public static ParsedCommand parse(String line) {
        return toCommand(tokenize(line == null ? "" : line));
    }
}
