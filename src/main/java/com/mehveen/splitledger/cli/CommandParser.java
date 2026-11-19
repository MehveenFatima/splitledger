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

    static List<String> tokenize(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        boolean hasToken = false;
        for (char c : line.toCharArray()) {
            if (c == '"') {
                quoted = !quoted;
                hasToken = true;
            } else if (Character.isWhitespace(c) && !quoted) {
                if (hasToken) {
                    tokens.add(current.toString());
                    current.setLength(0);
                    hasToken = false;
                }
            } else {
                current.append(c);
                hasToken = true;
            }
        }
        if (quoted) {
            throw new ValidationException("Missing closing quote");
        }
        if (hasToken) {
            tokens.add(current.toString());
        }
        return tokens;
    }
}
