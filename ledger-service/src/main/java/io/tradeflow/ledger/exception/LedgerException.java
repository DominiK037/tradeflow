package io.tradeflow.ledger.exception;

/**
 * Base exception for all ledger-service domain errors.
 *
 * <p>All ledger exceptions extend this class so callers can catch the entire
 * ledger error family with a single catch block when needed, while still
 * being able to handle specific subtypes individually.
 *
 * <p><b>Why RuntimeException:</b> ledger operations are invoked deep in the
 * call stack (engine → repository → gRPC handler). Checked exceptions would
 * force every intermediate layer to declare throws clauses, adding noise
 * with no safety benefit. Spring @Transactional rolls back on RuntimeException
 * by default — correct behaviour for all ledger failures.
 *
 * <p><b>Thread safety:</b> immutable after construction.
 *
 * <p><b>Spring context:</b> none — plain domain exception.
 */
public abstract class LedgerException extends RuntimeException {

    protected LedgerException(String message) {
        super(message);
    }

    protected LedgerException(String message, Throwable cause) {
        super(message, cause);
    }

}
