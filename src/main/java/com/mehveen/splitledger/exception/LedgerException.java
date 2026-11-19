package com.mehveen.splitledger.exception;

/**
 * Base type for all errors raised by SplitLedger business rules.
 * The CLI catches this type and shows the message to the user.
 */
public class LedgerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public LedgerException(String message) {
        super(message);
    }
}
