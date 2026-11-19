package com.mehveen.splitledger.settle;

import com.mehveen.splitledger.money.Money;

/**
 * A suggested payment that moves the group towards being settled.
 *
 * @param fromId member who should pay
 * @param toId   member who should receive
 */
public record Transfer(String fromId, String toId, Money amount) {
}
