# Changelog

All notable changes to this project are documented here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [1.0.0] - 2026-11-19

### Added
- `Money` value type with paise-exact arithmetic and largest-remainder allocation.
- Equal, exact, percent and shares split strategies.
- `Ledger` with members, expenses, payments, balances, category totals and filters.
- Greedy two-heap `DebtSimplifier` producing at most `n - 1` settle-up payments.
- Plain-text report and quick statistics.
- CSV save / load with line-numbered error messages.
- Interactive CLI and `--script` mode.
- 164 unit tests, GitHub Actions CI with coverage report.
