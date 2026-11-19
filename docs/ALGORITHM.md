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

## Worked example

Balances after a trip: Aman **+3933.33**, Riya **-2866.67**, Sam **-1066.66**.

| Step | Largest creditor | Largest debtor | Payment | Left over |
|---|---|---|---|---|
| 1 | Aman 3933.33 | Riya 2866.67 | Riya pays Aman 2866.67 | Aman +1066.66 |
| 2 | Aman 1066.66 | Sam 1066.66 | Sam pays Aman 1066.66 | nobody |

Two payments settle three people.

## Why it works

- **Correctness:** every payment moves money from someone who owes to someone who is owed, and
  never more than either of them needs, so no balance ever changes sign. The loop ends only when
  one heap is empty; because the total is zero, the other heap is then empty too.
- **At most n - 1 payments:** each payment removes at least one person from the heaps for good.
  The very last payment removes two people (the final creditor and debtor are equal), so with `n`
  people with a non-zero balance there are at most `n - 1` payments.

Note: finding the *absolute* minimum number of payments is NP-hard in general (it is related to
partitioning people into zero-sum groups). The greedy approach is the standard practical choice:
fast, simple and never worse than `n - 1`.

## Complexity

| | Cost |
|---|---|
| Building the heaps | O(n log n) |
| Each loop iteration | O(log n) heap operations |
| Number of iterations | at most n - 1 |
| **Total time** | **O(n log n)** |
| **Extra space** | **O(n)** |

## How it is tested

`DebtSimplifierTest` covers the simple cases (two people, one creditor with many debtors, chains)
and runs **randomized property tests**: for hundreds of random balanced inputs it applies the
returned payments and checks that every balance ends at exactly zero, that no payment is zero or
negative, and that there are never more than `n - 1` payments.
