package com.mehveen.splitledger.cli;

import com.mehveen.splitledger.exception.ValidationException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A command line split into its words, positional arguments and {@code --options}.
 * Example: {@code expense add Dinner 900 --payer riya} has words
 * {@code [expense, add, Dinner, 900]} and option {@code payer=riya}.
 */
public record ParsedCommand(List<String> words, Map<String, String> options) {

    public ParsedCommand {
        words = List.copyOf(words);
        options = Map.copyOf(options);
    }
}
