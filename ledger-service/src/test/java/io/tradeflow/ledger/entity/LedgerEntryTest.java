package io.tradeflow.ledger.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LedgerEntry} entity construction and field contract.
 *
 * <p>No Spring context, no database — pure Java construction tests.
 * Integration tests (task-1.9) verify that these entities persist correctly
 * against a real MySQL schema via Testcontainers.
 */
@DisplayName("LedgerEntry")
class LedgerEntryTest {

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        @DisplayName("sets all fields correctly when constructed with valid arguments")
        void setsAllFieldsCorrectlyWhenConstructedWithValidArguments() {
            Account account = new Account("user-123", "USD");
            LedgerEntry entry = new LedgerEntry(account, EntryType.DEBIT, 5000L, "uuid-abc", "Payment for order #1");

            assertAll(
                () -> assertEquals(account,        entry.getAccount()),
                () -> assertEquals(EntryType.DEBIT, entry.getEntryType()),
                () -> assertEquals(5000L,           entry.getAmountCents()),
                () -> assertEquals("uuid-abc",      entry.getReferenceId()),
                () -> assertEquals("Payment for order #1", entry.getDescription())
            );
        }

        @Test
        @DisplayName("returns null id before persistence")
        void returnsNullIdBeforePersistence() {
            LedgerEntry entry = new LedgerEntry(
                new Account("user-123", "USD"), EntryType.CREDIT, 5000L, "uuid-abc", "Credit");

            assertNull(entry.getId());
        }

        @Test
        @DisplayName("returns null createdAt before persistence")
        void returnsNullCreatedAtBeforePersistence() {
            LedgerEntry entry = new LedgerEntry(
                new Account("user-123", "USD"), EntryType.DEBIT, 1L, "uuid-xyz", "Debit");

            assertNull(entry.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("EntryType")
    class EntryTypeTests {

        @Test
        @DisplayName("DEBIT and CREDIT are the only permitted values")
        void debitAndCreditAreTheOnlyPermittedValues() {
            assertEquals(2, EntryType.values().length);
        }

        @Test
        @DisplayName("entry type is preserved correctly for CREDIT")
        void entryTypeIsPreservedCorrectlyForCredit() {
            LedgerEntry entry = new LedgerEntry(
                new Account("user-123", "USD"), EntryType.CREDIT, 5000L, "uuid-abc", "Credit");

            assertEquals(EntryType.CREDIT, entry.getEntryType());
        }
    }

    @Nested
    @DisplayName("immutability")
    class Immutability {

        @Test
        @DisplayName("exposes no setter methods")
        void exposesNoSetterMethods() {
            long setterCount = java.util.Arrays.stream(LedgerEntry.class.getMethods())
                .filter(m -> m.getName().startsWith("set"))
                .count();

            assertEquals(0, setterCount, "LedgerEntry must expose no setters — entries are immutable after insert");
        }
    }
}
