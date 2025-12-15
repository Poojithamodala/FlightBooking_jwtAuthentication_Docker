package com.demo.security;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            JwtBlacklistWebFilter jwtBlacklistWebFilter) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> {})
                .authorizeExchange(exchanges -> exchanges
                		.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/eureka/**").permitAll()

                        .pathMatchers(HttpMethod.POST, "/user-service/auth/register").permitAll()
                        .pathMatchers(HttpMethod.POST, "/user-service/auth/login").permitAll()
                        .pathMatchers(HttpMethod.POST, "/user-service/auth/logout").authenticated()
                        .pathMatchers(HttpMethod.GET, "/user-service/auth/token/blacklisted").permitAll()

                        .pathMatchers(HttpMethod.POST, "/flight-service/api/flight/search").permitAll()
                        .pathMatchers(HttpMethod.POST, "/flight-service/api/flight/search/airline").permitAll()
//                        .pathMatchers(HttpMethod.GET, "/flight-service/api/flight/search/airline").permitAll()
                        .pathMatchers(HttpMethod.GET, "/flight-service/api/flight/allflights").permitAll()
                        .pathMatchers(HttpMethod.GET, "/flight-service/api/flight/*").permitAll()

                        .pathMatchers("/booking-service/api/flight/booking/**").authenticated()
                        .pathMatchers("/booking-service/api/flight/ticket/**").authenticated()
                        .pathMatchers("/booking-service/api/flight/booking/history/**").authenticated()
                        .pathMatchers("/flight-service/api/flight/airline/inventory/add").authenticated()

                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt())
                .addFilterAfter(
                		jwtBlacklistWebFilter,
                        SecurityWebFiltersOrder.AUTHENTICATION
                )
                .build();
    }
    
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.secret}") String secret) {

        SecretKey key = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        return NimbusReactiveJwtDecoder.withSecretKey(key).build();
    }
}

//package com.demo.security;
//
//import java.nio.charset.StandardCharsets;
//import javax.crypto.SecretKey;
//import javax.crypto.spec.SecretKeySpec;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
//import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
//import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
//import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
//import org.springframework.security.web.server.SecurityWebFilterChain;
//import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
//
//@Configuration
//@EnableWebFluxSecurity
//@EnableReactiveMethodSecurity
//public class SecurityConfig {
//
//    // =========================
//    // 1️⃣ PUBLIC ENDPOINTS
//    // =========================
//    @Bean
//    @Order(1)
//    public SecurityWebFilterChain publicChain(ServerHttpSecurity http) {
//
//        return http
//            .securityMatcher(ServerWebExchangeMatchers.pathMatchers(
//                "/actuator/**",
//                "/eureka/**",
//
//                "/user-service/auth/register",
//                "/user-service/auth/login",
//                "/user-service/auth/token/blacklisted",
//
//                "/flight-service/api/flight/search",
//                "/flight-service/api/flight/search/airline",
//                "/flight-service/api/flight/allflights",
//                "/flight-service/api/flight/*"
//            ))
//            .csrf(ServerHttpSecurity.CsrfSpec::disable)
//            .authorizeExchange(ex -> ex.anyExchange().permitAll())
//            .build();
//    }
//
//    // =========================
//    // 2️⃣ PROTECTED ENDPOINTS
//    // =========================
//    @Bean
//    @Order(2)
//    public SecurityWebFilterChain protectedChain(
//            ServerHttpSecurity http,
//            JwtBlacklistWebFilter blacklistWebFilter) {
//
//        return http
//            .csrf(ServerHttpSecurity.CsrfSpec::disable)
//            .authorizeExchange(ex -> ex
//                .pathMatchers(HttpMethod.POST, "/user-service/auth/logout").authenticated()
//                .pathMatchers(HttpMethod.GET, "/user-service/auth/*").authenticated()
//
//                .pathMatchers("/booking-service/api/flight/booking/**").authenticated()
//                .pathMatchers("/booking-service/api/flight/ticket/**").authenticated()
//                .pathMatchers("/booking-service/api/flight/booking/history/**").authenticated()
//
//                .pathMatchers("/flight-service/api/flight/airline/inventory/add").authenticated()
//
//                .anyExchange().authenticated()
//            )
//            .oauth2ResourceServer(oauth2 -> oauth2.jwt())
//            .addFilterAfter(
//                blacklistWebFilter,
//                SecurityWebFiltersOrder.AUTHENTICATION
//            )
//            .build();
//    }
//
//    // =========================
//    // JWT DECODER
//    // =========================
//    @Bean
//    public ReactiveJwtDecoder reactiveJwtDecoder(
//            @Value("${spring.security.oauth2.resourceserver.jwt.secret}") String secret) {
//
//        SecretKey key = new SecretKeySpec(
//            secret.getBytes(StandardCharsets.UTF_8),
//            "HmacSHA256"
//        );
//
//        return NimbusReactiveJwtDecoder.withSecretKey(key).build();
//    }
//}
