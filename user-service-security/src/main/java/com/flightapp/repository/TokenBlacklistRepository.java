package com.flightapp.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.flightapp.model.BlacklistedToken;

import reactor.core.publisher.Mono;

@Repository
public interface TokenBlacklistRepository extends ReactiveMongoRepository<BlacklistedToken, String> {
    Mono<BlacklistedToken> findByToken(String token);
}