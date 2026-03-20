package io.tradeflow.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Order Management Service.
 *
 * <p>Bounded context: e-commerce order lifecycle.
 * Manages orders from creation (PENDING) through fund reservation (RESERVED)
 * to final confirmation (CONFIRMED) or cancellation (CANCELLED).
 *
 * <p><b>Why @EnableScheduling:</b> the Outbox Poller runs as a {@code @Scheduled}
 * Virtual Thread task that polls {@code outbox_events} and publishes to Kafka.
 * Scheduling must be explicitly enabled — Spring Boot does not activate it
 * by default to avoid accidental background task execution.
 *
 * <p><b>Thread safety:</b> application singleton — no shared mutable state
 * in this class.
 */
@SpringBootApplication
@EnableScheduling
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

}
