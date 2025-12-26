package com.flightapp.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.flightapp.messaging.PasswordResetToken;

import reactor.core.publisher.Mono;

@Repository
public interface PasswordResetTokenRepository extends ReactiveMongoRepository<PasswordResetToken, String> {

	Mono<PasswordResetToken> findByToken(String token);
}
