package io.tradeflow.ledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Ledger Service.
 *
 * <p>Bounded context: financial wallet management and double-entry bookkeeping.
 * This service owns all monetary state in TradeFlow. No other service reads
 * from or writes to the ledger database schema directly.
 *
 * <p><b>Why no @EnableScheduling:</b> ledger-service has no outbox poller.
 * It consumes order-events directly via @KafkaListener and produces
 * wallet-events via KafkaTemplate. The @Scheduled outbox pattern belongs
 * to order-service which owns the outbox_events table.
 *
 * <p><b>Thread safety:</b> application singleton — no shared mutable state.
 *
 * <p><b>Spring context:</b> singleton.
 */
@SpringBootApplication
public class LedgerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LedgerServiceApplication.class, args);
    }

}
