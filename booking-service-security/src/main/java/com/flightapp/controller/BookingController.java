package com.flightapp.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.model.FLIGHTTYPE;
import com.flightapp.model.Passenger;
import com.flightapp.model.Ticket;
import com.flightapp.service.BookingService;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/flight")
public class BookingController {

	private final BookingService bookingService;

	@Data
	public static class BookingRequest {
		private String userEmail;
		private String returnFlightId;
		private FLIGHTTYPE tripType;
		private List<@Valid Passenger> passengers;
	}

	@PostMapping("/booking/{departureFlightId}")
	public Mono<String> bookTicket(@PathVariable String departureFlightId, @RequestBody BookingRequest request) {
		return bookingService.bookTicket(request.getUserEmail(), departureFlightId, request.getReturnFlightId(),
				request.getPassengers(), request.getTripType());
	}

	@GetMapping("/ticket/{pnr}")
	public Mono<Ticket> getTicket(@PathVariable String pnr) {
		return bookingService.getByPnr(pnr);
	}

	@GetMapping("/booking/history/{emailId}")
	public Flux<Ticket> history(@PathVariable String emailId) {
		return bookingService.historyByEmail(emailId);
	}

	@DeleteMapping("/booking/cancel/{pnr}")
	public Mono<String> cancel(@PathVariable String pnr) {
		return bookingService.cancelByPnr(pnr);
	}
}
