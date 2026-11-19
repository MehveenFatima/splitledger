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

