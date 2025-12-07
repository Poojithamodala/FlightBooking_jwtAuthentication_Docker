package com.flightapp.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.flightapp.model.Ticket;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TicketRepository extends ReactiveMongoRepository<Ticket, String> {
	Mono<Ticket> findByPnr(String pnr);
	Flux<Ticket> findByUserEmail(String email);
}
