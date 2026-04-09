# Ledger Schema — Blind Spots & Future Considerations

Schema: `V1__init_schema.sql`
Reviewed: task-1.2

---

## 1. Unbalanced Double-Entry (No DB Enforcement)

**Risk:** A crash between the DEBIT INSERT and the CREDIT INSERT leaves money debited with no corresponding credit.

**Why the schema can't fix it:** No DB constraint can enforce "every DEBIT must have a matching CREDIT" across rows.

**Where it's handled:** `LedgerEngine` (task-1.4) — both inserts must be inside the same `@Transactional` boundary. A rollback removes both rows atomically.

**Status:** Deferred to task-1.4. No schema change needed.

---

## 2. No Idempotency Key Expiry (Unbounded Table Growth)

**Risk:** `idempotency_keys` grows indefinitely. At scale (millions of transactions/day) this degrades query performance and storage.

**Fix when needed:** Add `expires_at DATETIME(6)` column + a scheduled cleanup job that deletes rows past expiry.

**Status:** Deferred. Add in a later migration alongside the cleanup job.

---

## 3. `reference_id` Length (VARCHAR 64)

**Risk:** Assumes a standard UUID (36 chars). If `reference_id` ever needs to encode compound values (e.g. `{idempotency_key}:{operation}`), 64 chars may be tight.

**Fix when needed:** Increase to `VARCHAR(128)` in a new migration.

**Status:** Low risk. UUIDs fit comfortably. Monitor if reference format changes.

---

## 4. No Account Status / Lifecycle

**Risk:** No `status` column on `accounts`. Cannot suspend or close an account at the DB level — e.g. fraud flagging requires application-only enforcement.

**Fix when needed:**
```sql
ALTER TABLE accounts
    ADD COLUMN status ENUM('ACTIVE', 'SUSPENDED', 'CLOSED') NOT NULL DEFAULT 'ACTIVE';
```

**Status:** Deferred. Add when account lifecycle management is built.

---

## 5. Negative Balance Not Enforced at DB Level

**Risk:** The schema ensures `amount_cents > 0` per entry, but cannot prevent the derived balance (`SUM`) from going negative.

**Where it's handled:** `LedgerEngine` (task-1.4) — checks balance before inserting a DEBIT using `Isolation.REPEATABLE_READ` to prevent concurrent overdrafts.

**Status:** By design. DB triggers could enforce this but add hidden complexity. Application layer is the correct boundary.

---

## What the Schema Does Enforce

| Constraint | Mechanism |
|---|---|
| One account per user per currency | `UNIQUE KEY uq_accounts_user_currency` |
| Currency consistency across tables | Composite FK `(account_id, currency_code)` |
| Positive entry amounts | `CHECK (amount_cents > 0)` |
| No duplicate mutations | `UNIQUE KEY uq_idempotency_keys_key` |
| Valid JSON in response_body | `JSON` column type (MySQL 8) |
