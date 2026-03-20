package io.tradeflow.ledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Ledger Wallet Service.
 *
 * <p>Bounded context: financial wallet management and double-entry bookkeeping.
 * This service owns all monetary state in TradeFlow. No other service reads
 * from or writes to the ledger database schema directly.
 *
 * <p><b>Why @EnableScheduling:</b> the Transactional Outbox pattern requires
 * a background scheduler to poll the {@code outbox_events} table and relay
 * pending events to Kafka. Without this annotation, {@code @Scheduled} methods
 * are silently ignored by the Spring container.
 *
 * <p><b>Thread safety:</b> application singleton — no shared mutable state
 * in this class.
 */
@SpringBootApplication
@EnableScheduling
public class LedgerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LedgerServiceApplication.class, args);
    }

}
