package io.tradeflow.ledger.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * JPA entity representing a single row in the double-entry ledger.
 *
 * <p>Every financial operation (e.g. reserve funds, credit account) produces
 * exactly two {@code LedgerEntry} rows — one {@link EntryType#DEBIT} and one
 * {@link EntryType#CREDIT}. Both rows share the same {@code referenceId}
 * (the idempotency key of the originating request).
 *
 * <p><b>Why immutable after insert:</b> ledger entries are the audit trail.
 * Mutating a posted entry would corrupt the financial record. All fields
 * except the generated {@code id} and {@code createdAt} are set once at
 * construction and never changed — reflected by the absence of setters.
 *
 * <p><b>Why composite FK on (account_id, currency_code):</b> the schema enforces
 * that a ledger entry's currency always matches its account's currency at the
 * database level. A FK on {@code account_id} alone would allow inserting a USD
 * entry against an EUR account — a silent data corruption the application layer
 * might not catch. The composite FK makes that impossible.
 *
 * <p><b>Why FetchType.LAZY on account:</b> the most common query pattern is
 * summing {@code amountCents} for a given {@code account_id}. Eagerly loading
 * the full {@code Account} on every ledger entry fetch would be an unnecessary
 * JOIN on every balance calculation.
 *
 * <p><b>Thread safety:</b> not thread-safe — JPA entities are request-scoped.
 *
 * <p><b>Spring context:</b> none — managed by the JPA persistence context.
 */
@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The account this entry belongs to.
     *
     * <p>Mapped via a composite FK: both {@code account_id} and
     * {@code currency_code} reference {@code accounts(id, currency_code)}.
     * This mirrors the composite unique key added to the accounts table
     * specifically to serve as this FK target.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
        @JoinColumn(name = "account_id",    referencedColumnName = "id",            nullable = false),
        @JoinColumn(name = "currency_code", referencedColumnName = "currency_code", nullable = false)
    })
    private Account account;

    /**
     * Currency code, readable directly without loading the Account proxy.
     *
     * <p><b>Why insertable = false, updatable = false:</b> {@code currency_code}
     * is already managed by the {@code @JoinColumns} relationship above.
     * Declaring it again as a plain column would cause Hibernate to attempt
     * writing the same column twice, resulting in a MappingException.
     * Setting both flags to false tells Hibernate: "I want to read this column,
     * but the relationship mapping owns writes."
     */
    @Column(name = "currency_code", nullable = false, length = 3,
            insertable = false, updatable = false)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private EntryType entryType;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(name = "reference_id", nullable = false, length = 64)
    private String referenceId;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * No-arg constructor required by the JPA specification.
     *
     * <p><b>Why protected:</b> enforces that application code uses the
     * parameterised constructor, guaranteeing a fully valid entity state.
     */
    protected LedgerEntry() { }

    /**
     * Creates a new immutable ledger entry.
     *
     * @param account      the account this entry is posted against; must not be null
     * @param entryType    DEBIT or CREDIT; must not be null
     * @param amountCents  value of the entry in the smallest currency unit; must be positive
     * @param referenceId  idempotency key of the originating request; must not be null or blank
     * @param description  human-readable description for the audit log; must not be null or blank
     */
    public LedgerEntry(Account account,
                       EntryType entryType,
                       long amountCents,
                       String referenceId,
                       String description) {
        this.account = account;
        this.entryType = entryType;
        this.amountCents = amountCents;
        this.referenceId = referenceId;
        this.description = description;
    }

    public Long getId() {
        return this.id;
    }

    public Account getAccount() {
        return this.account;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public EntryType getEntryType() {
        return this.entryType;
    }

    public long getAmountCents() {
        return this.amountCents;
    }

    public String getReferenceId() {
        return this.referenceId;
    }

    public String getDescription() {
        return this.description;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
