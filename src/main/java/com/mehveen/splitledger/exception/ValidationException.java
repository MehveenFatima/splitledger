package com.mehveen.splitledger.exception;

/** Thrown when user input breaks a business rule (bad amount, unknown split, etc.). */
public class ValidationException extends LedgerException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
