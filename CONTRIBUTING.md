# Contributing to SplitLedger

Thanks for your interest! This guide explains how to propose a change.

## Setup

1. Install Java 17+ and Maven 3.8+.
2. Fork and clone the repository.
3. Run `mvn verify` and make sure everything passes before you start.

## Coding conventions

- Plain Java 17, no runtime dependencies.
- Never use `double` for money; use `Money`.
- Keep domain objects immutable where possible.
- Throw `ValidationException` / `NotFoundException` for user errors so the CLI can show them.
- Four-space indentation (see `.editorconfig`).

