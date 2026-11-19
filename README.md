# SplitLedger

[![CI](https://github.com/MehveenFatima/splitledger/actions/workflows/ci.yml/badge.svg)](https://github.com/MehveenFatima/splitledger/actions/workflows/ci.yml)
![Java 17](https://img.shields.io/badge/Java-17-blue)
![License: MIT](https://img.shields.io/badge/License-MIT-green)

**Split group expenses fairly and settle up with the fewest possible payments.**

SplitLedger is a command-line app written in plain Java 17 with **zero runtime dependencies**.

## Why?

After a trip with friends, everyone has paid for different things: one person booked flights,
another paid for dinner, someone else covered the villa. Working out who owes whom by hand is
slow and error-prone, and paying everyone back one by one creates far more transfers than needed.

SplitLedger records every expense, keeps an exact running balance for each person, and then
computes a **minimal settle-up plan**: with `n` people it never needs more than `n - 1` payments.

## Features

- **Four ways to split** an expense: equal, exact amounts, percentages and shares (e.g. 2:1:1)
- **Paise-exact money**: amounts are stored as whole paise, so ₹100 / 3 becomes 33.34 + 33.33 + 33.33 and nothing is ever lost to rounding
- **Smart settle-up**: a greedy two-heap algorithm turns balances into the fewest payments
- **Categories and reports**: spending per category, per person, top spender and biggest expense
- **Filters**: list expenses by member, category or date range
- **Save and load** a ledger as a readable CSV file
- **Script mode** to replay a list of commands (great for demos and CI smoke tests)
- **164 unit tests** covering money arithmetic, every split type, the settlement algorithm (including randomized property tests), storage and the CLI

## Quick start

Requirements: **Java 17+** and **Maven 3.8+**.

```bash
git clone https://github.com/MehveenFatima/splitledger.git
cd splitledger
mvn package                      # compiles and runs all tests
java -jar target/splitledger-1.0.0.jar
```

Or use the helper script, which builds the jar on first run:

```bash
./scripts/run.sh                                   # interactive
./scripts/run.sh examples/sample-ledger.csv        # open a saved ledger
./scripts/run.sh --script examples/demo-trip.txt   # run the Goa trip demo
```

## Example session

```text
splitledger> member add "Riya Sharma"
Added Riya Sharma (id: riya)
splitledger> member add "Aman Verma"
Added Aman Verma (id: aman)
splitledger> member add "Sam Khan"
Added Sam Khan (id: sam)
splitledger> expense add "Flight tickets" 18000 --payer aman --category travel --date 2026-11-14
Added expense #1 2026-11-14 Flight tickets Rs 18000.00 paid by aman [travel, equal]
  riya owes Rs 6000.00
  aman owes Rs 6000.00
  sam owes Rs 6000.00
splitledger> expense add "Villa rent" 12000 --payer sam --split shares --with riya:1,aman:1,sam:2
...
splitledger> settle
Settle everyone in 2 payment(s):
  riya pays aman Rs 2866.67
  sam pays aman Rs 1066.66
```

## Commands

| Command | What it does |
|---|---|
| `member add <name> [--id <handle>]` | Add a person. The id defaults to the first name in lowercase. |
| `member remove <id>` | Remove a person who has no expenses or payments. |
| `members` | List members with their current balance. |
| `expense add <description> <amount> --payer <id> [options]` | Record an expense (options below). |
| `expense remove <id>` | Delete an expense, e.g. `expense remove #3`. |
| `expenses [--member id] [--category c] [--from date] [--to date]` | List and filter expenses. |
| `pay <from> <to> <amount> [--date d]` | Record a settle-up payment. |
| `balances` | Who owes and who is owed, in plain words. |
| `settle` | The fewest payments that settle everyone. |
| `report` | Full summary: totals, balances, categories and settle-up plan. |
| `stats` | Total, average per member, top spender and biggest expense. |
| `save <file>` / `load <file>` | Store or restore the ledger. |
| `help` / `exit` | Show help / quit. |

### Expense options

| Option | Meaning | Default |
|---|---|---|
| `--split equal\|exact\|percent\|shares` | How to divide the amount | `equal` |
| `--with ...` | Participants: `a,b,c` for equal; `a:300,b:200` for the others | everyone (equal only) |
| `--category ...` | `food`, `travel`, `rent`, `utilities`, `shopping`, `entertainment`, `other` | `other` |
| `--date YYYY-MM-DD` | When it happened | today |

| Split | Example | Result for ₹1,200 |
|---|---|---|
| equal | `--with riya,aman,sam` | 400 / 400 / 400 |
| exact | `--with riya:700,aman:500` | 700 / 500 (must add up to 1,200) |
| percent | `--with riya:50,aman:25,sam:25` | 600 / 300 / 300 (must add up to 100) |
| shares | `--with riya:2,aman:1,sam:1` | 600 / 300 / 300 |

## How settle-up works

1. Every expense credits the payer with the full amount and debits each participant with their share.
   Payments credit the payer and debit the receiver. The balances always add up to zero.
2. Members with a positive balance (owed money) go into one max-heap, members with a negative
   balance (owe money) into another.
3. Repeatedly take the largest creditor and the largest debtor, make the debtor pay
   `min(owed, owing)`, and push back whoever still has something left.

Each step fully settles at least one person, so there are at most `n - 1` payments and the
algorithm runs in **O(n log n)** time. See [docs/ALGORITHM.md](docs/ALGORITHM.md) for a worked
example and the proof sketch.

## Project structure

```text
src/main/java/com/mehveen/splitledger
├── App.java              entry point (interactive and --script modes)
├── cli/                  command parsing and the command interpreter
├── exception/            LedgerException, ValidationException, NotFoundException
├── io/                   CSV codec and LedgerStore (save / load)
├── ledger/               Ledger: members, expenses, payments, balances, filters
├── model/                Member, Expense, Payment, Category
├── money/                Money value type (paise-exact arithmetic and allocation)
├── report/               plain-text report builder
├── settle/               DebtSimplifier (two-heap algorithm) and Transfer
└── split/                SplitStrategy + equal / exact / percent / shares strategies
```

More detail in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Design decisions

- **Money as `long` paise, never `double`.** Floating point cannot represent 0.10 exactly; integers can.
  Splitting uses the *largest remainder method* so the parts always add up to the original amount.
- **Strategy pattern for splits.** Each split type is a small class behind the `SplitStrategy`
  interface, and `AbstractSplitStrategy` (template method) holds the shared validation. Adding a new
  split type means adding one class and registering it in `SplitStrategyFactory`.
- **Immutable domain objects.** `Money`, `Member`, `Payment` and `Transfer` are immutable values and
  `Expense` cannot change after creation, which keeps the balance calculation simple and safe.
- **Deterministic output.** Ties in the settlement heaps are broken by member id, so the same
  ledger always produces the same plan (important for tests and for users).
- **No dependencies.** Even CSV handling is hand-written, so the jar runs anywhere Java 17 runs.

## Running the tests

```bash
mvn test        # 164 unit tests
mvn verify      # tests + JaCoCo coverage report in target/site/jacoco/index.html
```

The settlement algorithm also has randomized property tests: for hundreds of random balanced
inputs they check that the plan settles everyone and never uses more than `n - 1` payments.

## Continuous integration

GitHub Actions builds the project on every push and pull request (`.github/workflows/ci.yml`):
it runs all tests, runs the Goa trip demo script as a smoke test and uploads the coverage report.
Dependabot keeps Maven and Actions versions up to date.

## Roadmap

- [ ] Multiple currencies with conversion rates
- [ ] Recurring expenses (monthly rent, subscriptions)
- [ ] Export the report as Markdown or HTML
- [ ] REST API version with Spring Boot

