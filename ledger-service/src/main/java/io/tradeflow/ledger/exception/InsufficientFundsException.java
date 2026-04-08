package io.tradeflow.ledger.exception;

/**
 * Thrown when a fund reservation or debit is requested but the user's
 * available balance is below the requested amount.
 *
 * <p><b>Why carry userId, requestedCents, availableCents:</b> the GlobalExceptionHandler
 * maps these fields directly into the ProblemDetail response body. Downstream
 * clients (order-service, support tooling) receive the exact deficit without
 * parsing the message string — structured data, not string scraping.
 *
 * <p><b>Thread safety:</b> immutable after construction.
 *
 * <p><b>Spring context:</b> none — thrown by LedgerEngine, caught by
 * GlobalExceptionHandler and GrpcExceptionHandler.
 */
public class InsufficientFundsException extends LedgerException {

    private final String userId;
    private final long requestedCents;
    private final long availableCents;

    public InsufficientFundsException(String userId, long requestedCents, long availableCents) {
        super("User %s requested %d cents but only %d cents available"
                .formatted(userId, requestedCents, availableCents));
        this.userId = userId;
        this.requestedCents = requestedCents;
        this.availableCents = availableCents;
    }

    public String getUserId() {
        return this.userId;
    }

    public long getRequestedCents() {
        return this.requestedCents;
    }

    public long getAvailableCents() {
        return this.availableCents;
    }

}
