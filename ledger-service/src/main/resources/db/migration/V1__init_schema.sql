-- V1__init_schema.sql
-- TradeFlow Ledger Service — initial schema
-- Double-entry bookkeeping: balance is derived from ledger_entries, never stored on accounts.

-- -----------------------------------------------------------------------------
-- accounts
-- Represents a wallet for a user in a given currency.
-- No balance column — balance is always SUM(amount) FROM ledger_entries WHERE account_id = ?
-- -----------------------------------------------------------------------------
CREATE TABLE accounts (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       VARCHAR(64)  NOT NULL,
    currency_code CHAR(3)      NOT NULL,               -- ISO 4217: USD, EUR, GBP
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                        ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uq_accounts_user_currency (user_id, currency_code),
    UNIQUE KEY uq_accounts_id_currency (id, currency_code)      -- composite target for ledger_entries FK
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- ledger_entries
-- Every fund movement is an INSERT here — never an UPDATE on accounts.
-- One financial operation = two rows: one DEBIT + one CREDIT.
-- amount_cents is always positive; entry_type determines direction.
-- -----------------------------------------------------------------------------
CREATE TABLE ledger_entries (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    account_id       BIGINT        NOT NULL,
    entry_type       ENUM('DEBIT','CREDIT') NOT NULL,
    amount_cents     BIGINT        NOT NULL CHECK (amount_cents > 0),  -- smallest currency unit; always positive
    currency_code    CHAR(3)       NOT NULL,            -- ISO 4217
    reference_id     VARCHAR(64)   NOT NULL,            -- idempotency_key of the originating request
    description      VARCHAR(255)  NOT NULL,
    created_at       DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    CONSTRAINT fk_ledger_entries_account_currency
        FOREIGN KEY (account_id, currency_code) REFERENCES accounts (id, currency_code),
    INDEX idx_ledger_entries_account_id (account_id),
    INDEX idx_ledger_entries_reference_id (reference_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- idempotency_keys
-- Deduplication table for all mutating operations.
-- UNIQUE constraint on idempotency_key is the enforcement mechanism —
-- a duplicate INSERT fails at the DB level, not the application level.
-- response_body stores the original JSON response so duplicate requests
-- get the exact same response without re-executing the operation.
-- -----------------------------------------------------------------------------
CREATE TABLE idempotency_keys (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    idempotency_key  VARCHAR(64)   NOT NULL,
    operation        VARCHAR(64)   NOT NULL,            -- e.g. RESERVE_FUNDS, CREDIT_ACCOUNT
    response_body    JSON          NOT NULL,             -- validated JSON; MySQL 8 rejects malformed input
    created_at       DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uq_idempotency_keys_key (idempotency_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
