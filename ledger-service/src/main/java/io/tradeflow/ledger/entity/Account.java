package io.tradeflow.ledger.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA entity representing a user's wallet for a single currency.
 *
 * <p>One account per user per currency — enforced by the unique constraint
 * {@code uq_accounts_user_currency (user_id, currency_code)} in the schema.
 *
 * <p><b>Why no balance field:</b> balance is a derived value — always computed
 * as {@code SUM(ledger_entries)} for this account. Storing balance would require
 * an UPDATE on every transaction, creating a race condition under concurrency.
 * The source of truth is the append-only {@link LedgerEntry} table.
 *
 * <p><b>Why @CreationTimestamp / @UpdateTimestamp:</b> Hibernate intercepts
 * INSERT and UPDATE statements and injects the current JVM timestamp automatically.
 * No application code needs to set these fields — the framework guarantees them.
 *
 * <p><b>Thread safety:</b> not thread-safe — JPA entities are not shared across
 * threads. Each request receives its own entity instance from the persistence context.
 *
 * <p><b>Spring context:</b> none — managed by the JPA persistence context, not
 * the Spring application context.
 */
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * No-arg constructor required by the JPA specification.
     *
     * <p><b>Why protected, not public:</b> application code must use the
     * parameterised constructor to guarantee a valid entity state. JPA
     * (via reflection) can still instantiate it through the protected constructor.
     */
    protected Account() { }

    /**
     * Creates a new Account for a user in a given currency.
     *
     * @param userId        external user identifier; must not be null or blank
     * @param currencyCode  ISO 4217 currency code (e.g. "USD", "EUR"); must be 3 characters
     */
    public Account(String userId, String currencyCode) {
        this.userId = userId;
        this.currencyCode = currencyCode;
    }

    public Long getId() {
        return this.id;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
