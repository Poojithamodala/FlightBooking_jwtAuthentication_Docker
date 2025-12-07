package com.flightapp.repository;

import java.time.LocalDateTime;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.flightapp.model.Flight;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface FlightRepository extends ReactiveCrudRepository<Flight, String> {

	Flux<Flight> findByFromPlaceAndToPlaceAndDepartureTimeBetween(String fromPlace, String toPlace, LocalDateTime start,
			LocalDateTime end);

	Flux<Flight> findByFromPlaceAndToPlaceAndAirline(String fromPlace, String toPlace, String airline);

	Mono<Flight> findByAirlineAndFromPlaceAndToPlaceAndDepartureTime(String airline, String fromPlace, String toPlace,
			LocalDateTime departureTime);
}
