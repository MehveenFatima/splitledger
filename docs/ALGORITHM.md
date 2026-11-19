# Settle-up algorithm

## The problem

Given each member's **net balance** (positive = is owed, negative = owes, all balances add up to
zero), find a list of payments that brings every balance to zero using as few payments as possible.

Paying back expense by expense is wasteful. If Aman owes Riya ₹100 and Riya owes Sam ₹100, two
payments are not needed: Aman can pay Sam directly, and Riya is not involved at all.

