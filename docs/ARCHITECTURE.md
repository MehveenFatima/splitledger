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

