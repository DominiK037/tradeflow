package io.tradeflow.ledger.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Account} entity construction and field contract.
 *
 * <p>No Spring context, no database — pure Java construction tests.
 * Balance derivation and persistence are verified in task-1.9 IT tests.
 */
@DisplayName("Account")
class AccountTest {

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        @DisplayName("sets userId and currencyCode correctly when constructed with valid arguments")
        void setsUserIdAndCurrencyCodeCorrectlyWhenConstructedWithValidArguments() {
            Account account = new Account("user-123", "USD");

            assertAll(
                () -> assertEquals("user-123", account.getUserId()),
                () -> assertEquals("USD",      account.getCurrencyCode())
            );
        }

        @Test
        @DisplayName("returns null id before persistence")
        void returnsNullIdBeforePersistence() {
            Account account = new Account("user-123", "USD");

            assertNull(account.getId());
        }

        @Test
        @DisplayName("returns null timestamps before persistence")
        void returnsNullTimestampsBeforePersistence() {
            Account account = new Account("user-123", "USD");

            assertAll(
                () -> assertNull(account.getCreatedAt()),
                () -> assertNull(account.getUpdatedAt())
            );
        }
    }
}
