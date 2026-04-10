package io.tradeflow.ledger.entity;

/**
 * Direction of a ledger entry in the double-entry bookkeeping model.
 *
 * <p>Every financial operation produces exactly two {@code LedgerEntry} rows —
 * one {@code DEBIT} and one {@code CREDIT}. Both rows carry a positive
 * {@code amountCents}; this enum carries the direction.
 *
 * <p><b>Why an enum, not a boolean:</b> a boolean {@code isDebit} is ambiguous
 * at the call site ({@code true} means what, exactly?). An enum is self-documenting
 * and exhaustively checkable with a switch expression.
 *
 * <p><b>Thread safety:</b> enum constants are singletons and inherently thread-safe.
 *
 * <p><b>Spring context:</b> none — plain Java enum.
 */
public enum EntryType {

    /** Money leaves the account. */
    DEBIT,

    /** Money enters the account. */
    CREDIT
}
