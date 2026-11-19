package com.mehveen.splitledger.split;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * One participant's input to a split.
 *
 * @param memberId the participant
 * @param value    meaning depends on the {@link SplitType}: ignored for EQUAL,
 *                 rupees for EXACT, percent for PERCENT, share count for SHARES
 */
public record SplitShare(String memberId, BigDecimal value) {

    public SplitShare {
        Objects.requireNonNull(memberId, "memberId");
        memberId = memberId.trim().toLowerCase(java.util.Locale.ROOT);
        value = value == null ? BigDecimal.ZERO : value;
    }
}
