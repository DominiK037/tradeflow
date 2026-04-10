package io.tradeflow.ledger.repository;

import io.tradeflow.ledger.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link IdempotencyKey} entities.
 *
 * <p>The primary operation here is {@link #findByIdempotencyKey}, used by
 * {@code LedgerEngine} after catching a duplicate key exception on INSERT.
 * The engine fetches the stored record and returns the original response
 * to the caller without re-executing the operation.
 *
 * <p><b>Why the DB UNIQUE constraint is the first line of defence:</b>
 * an application-level duplicate check (SELECT before INSERT) has a race
 * condition — two concurrent requests can both pass the SELECT check and
 * both proceed to INSERT. The {@code UNIQUE KEY uq_idempotency_keys_key}
 * is atomic at the database level: only one INSERT wins; the other receives
 * a {@code DataIntegrityViolationException} that {@code LedgerEngine} catches.
 *
 * <p><b>Thread safety:</b> thread-safe — Spring Data generates a singleton proxy.
 *
 * <p><b>Spring context:</b> singleton.
 */
@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {

    /**
     * Finds a previously recorded idempotency record by its key.
     *
     * <p>Called by {@code LedgerEngine} after catching a
     * {@code DataIntegrityViolationException} on duplicate INSERT, to retrieve
     * the stored response body for the original request.
     *
     * @param idempotencyKey  the UUID from the client request; must not be null
     * @return                the stored record, or empty if not found
     */
    Optional<IdempotencyKey> findByIdempotencyKey(String idempotencyKey);
}
