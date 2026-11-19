# Architecture

SplitLedger is a small layered application. Each layer only depends on the layers below it.

```text
          App  (entry point: interactive / --script)
           │
          cli  (CommandParser → ParsedCommand → Cli)
           │
   ┌───────┼──────────┬───────────┐
 report   io        settle      ledger
   │       │          │           │
   └───────┴──────────┴─────┬─────┘
                       model, split
                            │
                          money
```

## Domain layer

- **`money.Money`**: immutable amount in paise with overflow-checked arithmetic and exact
  allocation (`allocate(n)` and `allocate(weights)` using the largest remainder method).
- **`model`**: `Member` (validated id + name), `Expense` (immutable, shares already computed and
  checked to add up to the amount), `Payment` and `Category`.
- **`split`**: `SplitStrategy` interface, `AbstractSplitStrategy` template with shared validation,
  four concrete strategies, and `SplitStrategyFactory` to pick one by `SplitType`.

## Ledger and settlement

- **`ledger.Ledger`** is the aggregate root: it owns members, expenses and payments, validates
  every reference (unknown members are rejected), and derives balances, totals and filters from
  the stored data instead of caching them, so they can never get out of sync.
- **`settle.DebtSimplifier`** is stateless: balances in, list of `Transfer`s out.
  See [ALGORITHM.md](ALGORITHM.md).

## Persistence and CLI

- **`io.CsvCodec`** encodes and decodes one CSV line with proper quoting.
- **`io.LedgerStore`** writes one record per line (`MEMBER`, `EXPENSE`, `PAYMENT`) and reports
  load errors with the line number. Expenses are stored with their computed shares, so loading
  never re-runs a split and always reproduces the same balances.
- **`cli.CommandParser`** handles quoted words and `--options`; **`cli.Cli`** maps commands to
  ledger calls. The CLI receives a `PrintStream` and a `Clock`, which makes it fully unit-testable.

## Error handling

All business rule violations throw a subclass of `LedgerException`
(`ValidationException`, `NotFoundException`). The CLI catches these, prints `Error: <message>`
and keeps running, so a typo never loses the user's data. Programming errors (for example a split
whose parts do not add up) throw `IllegalStateException` and are covered by tests.

