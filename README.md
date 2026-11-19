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

