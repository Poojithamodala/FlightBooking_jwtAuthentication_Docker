//package com.demo.security;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ResponseStatusException;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.WebFilter;
//import org.springframework.web.server.WebFilterChain;
//
//import reactor.core.publisher.Mono;
//
//@Component
//public class BlacklistTokenFilter implements WebFilter {
//
//    private final TokenBlacklistRepository blacklistRepository;
//
//    public BlacklistTokenFilter(TokenBlacklistRepository blacklistRepository) {
//        this.blacklistRepository = blacklistRepository;
//    }
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//
//        String authHeader = exchange.getRequest()
//                .getHeaders()
//                .getFirst(HttpHeaders.AUTHORIZATION);
//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            return chain.filter(exchange);
//        }
//
//        String token = authHeader.substring(7);
//
//        return blacklistRepository.findByToken(token)
//            .flatMap(t -> Mono.error(
//                new ResponseStatusException(
//                    HttpStatus.UNAUTHORIZED,
//                    "Token is blacklisted"
//                )))
//            .switchIfEmpty(chain.filter(exchange));
//    }
//}

package com.demo.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.demo.service.TokenBlacklistService;

import reactor.core.publisher.Mono;

@Component
public class JwtBlacklistWebFilter implements WebFilter {

    private final TokenBlacklistService tokenBlacklistService;

    public JwtBlacklistWebFilter(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String path = exchange.getRequest().getPath().value();
        HttpMethod method = exchange.getRequest().getMethod();

        // ✅ 1. Allow preflight requests
        if (method == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        if (path.startsWith("/user-service/auth/login")
                || path.startsWith("/user-service/auth/register")
                || path.startsWith("/user-service/auth/token/blacklisted")
                || path.startsWith("/flight-service/api/flight/search/airline")) {

            return chain.filter(exchange);
        }
//        if (path.contains("/auth/login")
//                || path.contains("/auth/register")
//                || path.contains("/auth/logout")
//                || path.contains("/auth/token/blacklisted")) {
//            return chain.filter(exchange);
//        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        return tokenBlacklistService.isBlacklisted(token)
                .flatMap(isBlacklisted -> {
                    if (isBlacklisted) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                    return chain.filter(exchange);
                });
    }
}
