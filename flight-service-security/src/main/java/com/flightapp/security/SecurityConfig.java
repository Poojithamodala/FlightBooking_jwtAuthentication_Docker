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
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.authorizeExchange(exchanges -> exchanges
						.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.pathMatchers("/actuator/**").permitAll()
						.pathMatchers(HttpMethod.POST, "/api/flight/search").permitAll()
						.pathMatchers(HttpMethod.POST, "/api/flight/search/airline").permitAll()
//						.pathMatchers(HttpMethod.GET, "/flight-service/api/flight/search/airline").permitAll()
						.pathMatchers(HttpMethod.GET, "/api/flight/allflights").permitAll()
						.pathMatchers(HttpMethod.GET, "/api/flight/*").permitAll()
						.pathMatchers(HttpMethod.POST, "/api/flight/airline/inventory/add").authenticated()
						.anyExchange().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt())
				.build();
	}

	@Bean
	public ReactiveJwtDecoder reactiveJwtDecoder(
			@Value("${spring.security.oauth2.resourceserver.jwt.secret}") String secret) {
		SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
		return NimbusReactiveJwtDecoder.withSecretKey(key).build();
	}
}
