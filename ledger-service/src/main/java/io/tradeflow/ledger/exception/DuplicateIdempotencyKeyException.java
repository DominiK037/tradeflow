package io.tradeflow.ledger.exception;

/**
 * Thrown when a mutation request arrives with an idempotency key that has
 * already been processed and stored in the idempotency_keys table.
 *
 * <p>This exception signals that the caller should receive the original
 * response, not a second execution. The GlobalExceptionHandler maps this
 * to 409 Conflict so callers can distinguish a true duplicate from other errors.
 *
 * <p><b>Why a dedicated exception not a silent duplicate suppression:</b>
 * the LedgerEngine returns the original response for true duplicates. This
 * exception is thrown only when the idempotency key exists but the original
 * response cannot be reconstructed — indicating a data integrity issue
 * that must surface, not be swallowed.
 *
 * <p><b>Thread safety:</b> immutable after construction.
 *
 * <p><b>Spring context:</b> none.
 */
public class DuplicateIdempotencyKeyException extends LedgerException {

    private final String idempotencyKey;

    public DuplicateIdempotencyKeyException(String idempotencyKey) {
        super("Idempotency key already processed: " + idempotencyKey);
        this.idempotencyKey = idempotencyKey;
    }

    public String getIdempotencyKey() {
        return this.idempotencyKey;
    }

}
