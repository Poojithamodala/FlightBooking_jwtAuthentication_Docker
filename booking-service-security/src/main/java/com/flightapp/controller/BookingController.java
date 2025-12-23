package com.flightapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.model.BookingHistoryResponse;
import com.flightapp.model.FLIGHTTYPE;
import com.flightapp.model.Passenger;
import com.flightapp.model.Ticket;
import com.flightapp.service.BookingService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/flight")
public class BookingController {

	private final BookingService bookingService;

	public BookingController(BookingService bookingService) {
		this.bookingService = bookingService;
	}

	@Data
	public static class BookingRequest {
		private String returnFlightId;

		@NotNull(message = "Trip type is required")
		private FLIGHTTYPE tripType;

		@NotEmpty(message = "At least one passenger is required")
		private List<@Valid Passenger> passengers;
	}

	@PostMapping("/booking/{departureFlightId}")
	public Mono<ResponseEntity<String>> bookTicket(@PathVariable String departureFlightId,
			@Valid @RequestBody BookingRequest request, @RequestHeader("Authorization") String authHeader) {

		return bookingService.bookTicket(departureFlightId, request.getReturnFlightId(), request.getPassengers(),
				request.getTripType(), authHeader).map(response -> ResponseEntity.status(201).body(response));
	}

	@GetMapping("/ticket/{pnr}")
	public Mono<Ticket> getTicket(@PathVariable String pnr) {
		return bookingService.getByPnr(pnr);
	}

//	@GetMapping("/booking/history")
//	public Flux<Ticket> history(Authentication authentication) {
//	    return bookingService.history(authentication);
//	}
	
	@GetMapping("/booking/history")
	public Flux<BookingHistoryResponse> myHistory(JwtAuthenticationToken auth) {
	    String email = auth.getToken().getSubject();
	    return bookingService.historyByEmail(email, auth.getToken().getTokenValue());
	}

	@DeleteMapping("/booking/cancel/{pnr}")
	public Mono<String> cancel(@PathVariable String pnr, @RequestHeader("Authorization") String token) {
		return bookingService.cancelByPnr(pnr, token);
	}
}
