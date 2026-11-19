package com.mehveen.splitledger.io;

import com.mehveen.splitledger.exception.LedgerException;
import com.mehveen.splitledger.ledger.Ledger;
import com.mehveen.splitledger.model.Category;
import com.mehveen.splitledger.model.Expense;
import com.mehveen.splitledger.model.Member;
import com.mehveen.splitledger.model.Payment;
import com.mehveen.splitledger.money.Money;
import com.mehveen.splitledger.split.SplitType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Saves and loads a ledger as a simple CSV file. Each line starts with a record type:
 * <pre>
 * MEMBER,riya,Riya Sharma
 * EXPENSE,1,2026-11-19,riya,1200.00,food,equal,Dinner,riya=600.00;sam=600.00
 * PAYMENT,2026-11-20,sam,riya,600.00
 * </pre>
 */
public class LedgerStore {

    static final String HEADER = "# SplitLedger v1";

    public void save(Ledger ledger, Path file) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Member member : ledger.members()) {
            lines.add(CsvCodec.encode(List.of("MEMBER", member.id(), member.name())));
        }
        for (Expense expense : ledger.expenses()) {
            lines.add(encodeExpense(expense));
        }
        for (Payment payment : ledger.payments()) {
            lines.add(CsvCodec.encode(List.of("PAYMENT", payment.date().toString(),
                    payment.fromId(), payment.toId(), payment.amount().toString())));
        }
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save ledger to " + file, e);
        }
    }

    private static String encodeExpense(Expense expense) {
        StringBuilder shares = new StringBuilder();
        expense.shares().forEach((id, amount) -> {
            if (shares.length() > 0) {
                shares.append(';');
            }
            shares.append(id).append('=').append(amount);
        });
        return CsvCodec.encode(List.of("EXPENSE", String.valueOf(expense.id()), expense.date().toString(),
                expense.payerId(), expense.amount().toString(), expense.category().label(),
                expense.splitType().name().toLowerCase(), expense.description(), shares.toString()));
    }

    public Ledger load(Path file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read ledger from " + file, e);
        }
        Ledger ledger = new Ledger();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).strip();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            try {
                applyRecord(ledger, CsvCodec.decode(line));
            } catch (RuntimeException e) {
                throw new LedgerException("Line " + (i + 1) + " of " + file.getFileName() + ": " + e.getMessage());
            }
        }
        return ledger;
    }

    private static void applyRecord(Ledger ledger, List<String> fields) {
        String type = fields.get(0);
        switch (type) {
            case "MEMBER" -> {
                requireFields(fields, 3);
                ledger.addMember(new Member(fields.get(1), fields.get(2)));
            }
            case "EXPENSE" -> ledger.restoreExpense(decodeExpense(fields));
            case "PAYMENT" -> {
                requireFields(fields, 5);
                ledger.recordPayment(fields.get(2), fields.get(3), Money.parse(fields.get(4)), date(fields.get(1)));
            }
            default -> throw new IllegalArgumentException("Unknown record type '" + type + "'");
        }
    }
}
