package io.tradeflow.ledger.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPublicKey;

/**
 * Central Spring configuration for the Ledger Service.
 *
 * <p>Owns two concerns: RSA public key loading and the HTTP security filter chain.
 * Both are placed here because they share the same dependency — the RSA public key
 * is produced here and consumed immediately by the SecurityFilterChain bean.
 *
 * <p><b>Why @EnableMethodSecurity:</b> activates @PreAuthorize on service methods,
 * allowing role checks at the method level in addition to URL-level rules.
 * Required for fine-grained RBAC inside gRPC service methods which bypass
 * the HTTP filter chain entirely.
 *
 * <p><b>Why RsaKeyConverters not a manual PEM parser:</b> Spring Security ships
 * a built-in X.509 PEM parser. Writing a custom one introduces parsing bugs
 * and does not handle edge cases (line endings, header variants). Use the
 * framework utility.
 *
 * <p><b>Thread safety:</b> all @Bean methods produce singletons. RSAPublicKey
 * is immutable. SecurityFilterChain is stateless. Safe for concurrent use.
 *
 * <p><b>Spring context:</b> singleton.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class AppConfig {

    /**
     * Loads the RSA public key from a PEM file at startup.
     *
     * <p><b>Why @Bean not @PostConstruct:</b> declaring the key as a @Bean makes
     * it a first-class Spring-managed object — injectable into any other bean,
     * lazily replaceable in tests, and clearly owned by the container. A
     * @PostConstruct field assignment is hidden state on this class; a @Bean
     * method is an explicit contract visible to the whole application context.
     *
     * <p><b>Why load at startup not per-request:</b> PEM parsing is expensive.
     * The public key is immutable — there is no reason to re-parse it on every
     * JWT validation. Loading once at startup and reusing the singleton is correct.
     *
     * @param location  classpath or file-system path to the RSA public key PEM,
     *                  injected from {@code app.security.rsa.public-key-location}
     * @return          the parsed RSAPublicKey, ready for use in NimbusJwtDecoder
     * @throws IOException if the PEM file cannot be read
     */
    @Bean
    public RSAPublicKey rsaPublicKey(
            @Value("${app.security.rsa.public-key-location}") Resource location)
            throws IOException {
        try (InputStream inputStream = location.getInputStream()) {
            return (RSAPublicKey) RsaKeyConverters.x509().convert(inputStream);
        }
    }

    /**
     * Configures the HTTP Security Filter Chain for the Ledger Service.
     *
     * <p>Stateless JWT API — no session, no CSRF, no form login.
     * Every request must carry a valid Bearer token issued by gateway-service.
     *
     * <p><b>Why CSRF disabled:</b> CSRF attacks exploit cookie-based authentication.
     * JWTs travel in the Authorization header — browsers do not attach headers
     * automatically on cross-site requests. No cookie, no CSRF risk.
     *
     * <p><b>Why STATELESS session:</b> Spring Security must not create an HttpSession.
     * Sessions introduce server-side state, breaking horizontal scaling and
     * conflicting with the JWT-based identity model.
     *
     * <p><b>Why NimbusJwtDecoder.withPublicKey:</b> ledger-service only validates
     * tokens — it never issues them. The public key is sufficient for signature
     * verification. The private key never leaves gateway-service.
     *
     * @param http          the HttpSecurity builder provided by Spring Security
     * @param rsaPublicKey  the RSA public key bean for JWT signature verification
     * @return              the built and immutable SecurityFilterChain
     * @throws Exception    if HttpSecurity configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                    RSAPublicKey rsaPublicKey)
            throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(
                    NimbusJwtDecoder.withPublicKey(rsaPublicKey).build())));

        return http.build();
    }

}
