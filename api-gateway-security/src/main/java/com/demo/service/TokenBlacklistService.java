package com.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class TokenBlacklistService {

    private final WebClient webClient;

    public TokenBlacklistService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    public Mono<Boolean> isBlacklisted(String token) {
        return webClient.get()
            .uri("http://user-service/auth/token/blacklisted?token=" + token)
            .retrieve()
            .bodyToMono(Boolean.class)
            .onErrorReturn(false); 
    }
}

