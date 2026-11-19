# Settle-up algorithm

## The problem

Given each member's **net balance** (positive = is owed, negative = owes, all balances add up to
zero), find a list of payments that brings every balance to zero using as few payments as possible.

Paying back expense by expense is wasteful. If Aman owes Riya ₹100 and Riya owes Sam ₹100, two
payments are not needed: Aman can pay Sam directly, and Riya is not involved at all.

## Step 1: net balances

For every expense, the payer is credited the full amount and every participant is debited
their share (the payer can be a participant too). For every payment, the payer is credited and
the receiver is debited. Only the net balance per person matters for settling up; the history of
who paid for what does not.

Because every credit has a matching debit, the balances always add up to exactly zero.
`DebtSimplifier` checks this and refuses unbalanced input.

## Step 2: greedy matching with two max-heaps

```text
creditors = max-heap of people who are owed money (by amount)
debtors   = max-heap of people who owe money (by amount)

while both heaps are not empty:
    c = creditors.poll()          # owed the most
    d = debtors.poll()            # owes the most
    x = min(c.amount, d.amount)
    record "d pays c x"
    if c.amount > x: push c back with c.amount - x
    if d.amount > x: push d back with d.amount - x
```

Ties are broken by member id so the output is deterministic.

