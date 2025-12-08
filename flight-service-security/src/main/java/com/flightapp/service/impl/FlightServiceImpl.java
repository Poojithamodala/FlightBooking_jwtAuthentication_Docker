package com.flightapp.service.impl;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.flightapp.model.Flight;
import com.flightapp.repository.FlightRepository;
import com.flightapp.service.FlightService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FlightServiceImpl implements FlightService {

	private static final String FLIGHT_NOT_FOUND = "Flight not found";
	private static final String FLIGHT_ALREADY_EXISTS = "Flight already exists";
	private static final String NOT_ENOUGH_SEATS = "Not enough seats";

	private final FlightRepository flightRepository;

	public FlightServiceImpl(FlightRepository flightRepository) {
		this.flightRepository = flightRepository;
	}

	@Override
	public Mono<Flight> addFlight(Flight flight) {
		return flightRepository.findByAirlineAndFromPlaceAndToPlaceAndDepartureTime(flight.getAirline(),
				flight.getFromPlace(), flight.getToPlace(), flight.getDepartureTime()).hasElement().flatMap(exists -> {
					if (exists) {
						return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Flight already exists"));
					}
					if (flight.getDepartureTime().isBefore(LocalDateTime.now())) {
						return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
								"Departure time cannot be in the past"));
					}
					if (flight.getTotalSeats() <= 0) {
						return Mono.error(
								new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seats must be greater than zero"));
					}
					return flightRepository.save(flight);
				}).onErrorMap(e -> {
					return e;
				});
	}

	@Override
	public Flux<Flight> getAllFlights() {
		return flightRepository.findAll();
	}

	@Override
	public Mono<Flight> searchFlightById(String flightId) {
		return flightRepository.findById(flightId).switchIfEmpty(Mono.error(new RuntimeException(FLIGHT_NOT_FOUND)));
	}

	@Override
	public Flux<Flight> searchFlights(String from, String to, LocalDateTime start, LocalDateTime end) {
		return flightRepository.findByFromPlaceAndToPlaceAndDepartureTimeBetween(from, to, start, end);
	}

	@Override
	public Flux<Flight> searchFlightsByAirline(String fromPlace, String toPlace, String airline) {
		return flightRepository.findByFromPlaceAndToPlaceAndAirline(fromPlace, toPlace, airline);
	}

	@Override
	public Mono<Flight> reserveSeats(String flightId, int seatCount) {
		return flightRepository.findById(flightId).switchIfEmpty(Mono.error(new RuntimeException(FLIGHT_NOT_FOUND)))
				.flatMap(flight -> {
					if (flight.getAvailableSeats() < seatCount) {
						return Mono.error(new RuntimeException(NOT_ENOUGH_SEATS));
					}
					flight.setAvailableSeats(flight.getAvailableSeats() - seatCount);
					return flightRepository.save(flight);
				});
	}

	@Override
	public Mono<Flight> releaseSeats(String flightId, int seatCount) {
		return flightRepository.findById(flightId).switchIfEmpty(Mono.error(new RuntimeException(FLIGHT_NOT_FOUND)))
				.flatMap(flight -> {
					flight.setAvailableSeats(flight.getAvailableSeats() + seatCount);
					return flightRepository.save(flight);
				});
	}
}