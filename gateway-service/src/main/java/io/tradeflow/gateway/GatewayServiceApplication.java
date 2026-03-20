package io.tradeflow.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the API Gateway and Real-Time Service.
 *
 * <p>This service runs on the <b>reactive stack</b> (Spring WebFlux + Netty).
 * It is the sole ingress point for all external traffic. No client communicates
 * directly with ledger-service or order-service.
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *   <li>JWT issuance — {@code POST /auth/login}</li>
 *   <li>Global JWT validation — rejects unauthenticated requests at the edge</li>
 *   <li>Reverse proxy routing — forwards REST requests to downstream services</li>
 *   <li>Redis rate limiting — sliding window per client IP / userId</li>
 *   <li>WebSocket hub — pushes real-time order and wallet events to browsers</li>
 * </ul>
 *
 * <p><b>Why no @EnableScheduling here:</b> the gateway has no scheduled tasks.
 * Kafka consumption and WebSocket push are event-driven, not polled.
 *
 * <p><b>Thread safety:</b> application singleton — no shared mutable state
 * in this class.
 */
@SpringBootApplication
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }

}
