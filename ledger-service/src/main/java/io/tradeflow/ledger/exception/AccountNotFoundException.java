package io.tradeflow.ledger.exception;

/**
 * Thrown when a ledger operation targets a userId that has no account record.
 *
 * <p><b>Thread safety:</b> immutable after construction.
 *
 * <p><b>Spring context:</b> none — thrown by LedgerEngine, mapped to 404 by
 * GlobalExceptionHandler.
 */
public class AccountNotFoundException extends LedgerException {

    private final String userId;

    public AccountNotFoundException(String userId) {
        super("No account found for user: " + userId);
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

}
