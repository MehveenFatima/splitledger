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

