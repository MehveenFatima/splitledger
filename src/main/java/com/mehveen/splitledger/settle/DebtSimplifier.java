package com.mehveen.splitledger.settle;

import com.mehveen.splitledger.money.Money;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Turns net balances into a short list of payments that settles everyone.
 *
 * <p>Greedy algorithm with two max-heaps: repeatedly match the member who is
 * owed the most with the member who owes the most, and transfer the smaller of
 * the two amounts. Each step settles at least one member completely, so with
 * {@code n} members there are at most {@code n - 1} transfers.</p>
 *
 * <p>Time complexity: O(n log n). Space complexity: O(n).</p>
 */
public class DebtSimplifier {

    /** A member's outstanding amount in paise (always positive inside a heap). */
    private record Position(String memberId, long paise) {
    }

    /** Largest amount first; ties broken by member id so results are deterministic. */
    private static final Comparator<Position> LARGEST_FIRST =
            Comparator.comparingLong(Position::paise).reversed().thenComparing(Position::memberId);

    private static void requireBalanced(Map<String, Money> balances) {
        long sum = 0;
        for (Money balance : balances.values()) {
            sum = Math.addExact(sum, balance.paise());
        }
        if (sum != 0) {
            throw new IllegalArgumentException("Balances must add up to zero but add up to " + Money.ofPaise(sum));
        }
    }

    public List<Transfer> simplify(Map<String, Money> balances) {
        requireBalanced(balances);
        PriorityQueue<Position> creditors = new PriorityQueue<>(LARGEST_FIRST);
        PriorityQueue<Position> debtors = new PriorityQueue<>(LARGEST_FIRST);
        balances.forEach((id, balance) -> {
            if (balance.isPositive()) {
                creditors.add(new Position(id, balance.paise()));
            } else if (balance.isNegative()) {
                debtors.add(new Position(id, -balance.paise()));
            }
        });

        List<Transfer> transfers = new ArrayList<>();
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Position creditor = creditors.poll();
            Position debtor = debtors.poll();
            long amount = Math.min(creditor.paise(), debtor.paise());
            transfers.add(new Transfer(debtor.memberId(), creditor.memberId(), Money.ofPaise(amount)));
            if (creditor.paise() > amount) {
                creditors.add(new Position(creditor.memberId(), creditor.paise() - amount));
            }
            if (debtor.paise() > amount) {
                debtors.add(new Position(debtor.memberId(), debtor.paise() - amount));
            }
        }
        return List.copyOf(transfers);
    }

    public static Money totalMoved(List<Transfer> transfers) {
        return transfers.stream().map(Transfer::amount).reduce(Money.ZERO, Money::plus);
    }
}
