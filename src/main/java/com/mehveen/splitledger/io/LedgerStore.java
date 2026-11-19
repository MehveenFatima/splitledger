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
}
