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

