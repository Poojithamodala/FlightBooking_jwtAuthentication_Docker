package com.flightapp.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.dto.FlightSearchRequest;
import com.flightapp.model.Flight;
import com.flightapp.service.FlightService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/flight")
public class FlightController {

	private final FlightService flightService;

	@PostMapping("/airline/inventory/add")
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Map<String, String>> addInventory(@Valid @RequestBody Flight flight) {
		return flightService.addFlight(flight)
				.map(savedFlight -> Map.of("message", "Flight added successfully", "flightId", savedFlight.getId()));
	}

	@PostMapping("/search")
	public Flux<Flight> searchFlights(@RequestBody FlightSearchRequest request) {
		return flightService.searchFlights(request.getFromPlace(), request.getToPlace(), request.getStartTime(),
				request.getEndTime());
	}

	@PostMapping("/search/airline")
	public Flux<Flight> searchByAirline(@RequestBody Map<String, String> body) {
		return flightService.searchFlightsByAirline(body.get("fromPlace"), body.get("toPlace"), body.get("airline"));
	}
//	@GetMapping("/search/airline")
//	public Flux<Flight> searchByAirline(
//	        @RequestParam String from,
//	        @RequestParam String to,
//	        @RequestParam String airline) {
//
//	    return flightService.searchFlightsByAirline(from, to, airline);
//	}

	@GetMapping("/{id}")
	public Mono<Flight> getFlightById(@PathVariable String id) {
		return flightService.searchFlightById(id);
	}

	@GetMapping("/allflights")
	public Flux<Flight> getAllFlights() {
		return flightService.getAllFlights();
	}

	@PutMapping("/internal/{id}/reserve/{seatCount}")
	public Mono<Flight> reserveSeats(@PathVariable String id, @PathVariable int seatCount) {
		return flightService.reserveSeats(id, seatCount);
	}

	@PutMapping("/internal/{id}/release/{seatCount}")
	public Mono<Flight> releaseSeats(@PathVariable String id, @PathVariable int seatCount) {
		return flightService.releaseSeats(id, seatCount);
	}
}
