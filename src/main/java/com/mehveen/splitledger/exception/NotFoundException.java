package com.mehveen.splitledger.exception;

/** Thrown when a member or expense referenced by id does not exist. */
public class NotFoundException extends LedgerException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message);
    }
}
