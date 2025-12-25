package com.flightapp.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain security(ServerHttpSecurity http) {

        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges

                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers(HttpMethod.POST, "/auth/register").permitAll()
                .pathMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .pathMatchers(HttpMethod.GET, "/auth/profile").authenticated()
                .pathMatchers(HttpMethod.PUT, "/auth/changepassword").authenticated()
                .pathMatchers(HttpMethod.POST, "/auth/logout").permitAll()
                .pathMatchers(HttpMethod.GET, "/auth/token/blacklisted").permitAll()

                .anyExchange().access((authentication, context) ->
                authentication.map(auth -> {

                    // If JWT exists, check claim
                    if (auth instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwtAuth) {

                        Boolean forceChange =
                            jwtAuth.getToken().getClaim("forcePasswordChange");

                        // ❌ Block everything except changepassword
                        if (Boolean.TRUE.equals(forceChange)) {
                            return new org.springframework.security.authorization.AuthorizationDecision(false);
                        }
                    }

                    // ✅ Allow normal access
                    return new org.springframework.security.authorization.AuthorizationDecision(true);
                })
            )
        )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt())
            .build();
    }
    
    @Bean
	public ReactiveJwtDecoder reactiveJwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.secret}") String secret) {
		SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
		return NimbusReactiveJwtDecoder.withSecretKey(key).build();
	}
}
