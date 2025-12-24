package com.flightapp.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.flightapp.exception.InvalidFlightException;
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

	    return flightRepository
	        .findByAirlineAndFromPlaceAndToPlaceAndDepartureTime(
	            flight.getAirline(),
	            flight.getFromPlace(),
	            flight.getToPlace(),
	            flight.getDepartureTime()
	        )
	        .hasElement()
	        .flatMap(exists -> {

	            if (exists) {
	                return Mono.error(new InvalidFlightException("Flight already exists"));
	            }

	            if (flight.getFromPlace().equalsIgnoreCase(flight.getToPlace())) {
	                return Mono.error(new InvalidFlightException(
	                    "From and To locations cannot be the same"
	                ));
	            }

	            if (flight.getDepartureTime().isBefore(LocalDateTime.now())) {
	                return Mono.error(new InvalidFlightException(
	                    "Departure must be in the future"
	                ));
	            }

	            if (flight.getArrivalTime().isBefore(flight.getDepartureTime())) {
	                return Mono.error(new InvalidFlightException(
	                    "Arrival time must be after departure time"
	                ));
	            }

	            if (flight.getTotalSeats() <= 0) {
	                return Mono.error(new InvalidFlightException(
	                    "Total seats must be greater than zero"
	                ));
	            }

	            if (flight.getAvailableSeats() < 0) {
	                return Mono.error(new InvalidFlightException(
	                    "Available seats cannot be negative"
	                ));
	            }

	            if (flight.getAvailableSeats() > flight.getTotalSeats()) {
	                return Mono.error(new InvalidFlightException(
	                    "Available seats cannot exceed total seats"
	                ));
	            }

	            if (flight.getPrice() <= 0) {
	                return Mono.error(new InvalidFlightException(
	                    "Price must be greater than zero"
	                ));
	            }

	            flight.setAirline(flight.getAirline().trim());
	            flight.setFromPlace(flight.getFromPlace().trim());
	            flight.setToPlace(flight.getToPlace().trim());

	            return flightRepository.save(flight);
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