package com.flightapp.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.flightapp.model.BookingHistoryResponse;
import com.flightapp.model.FLIGHTTYPE;
import com.flightapp.model.Passenger;
import com.flightapp.model.Ticket;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookingService {

	Mono<String> bookTicket( String departureFlightId, String returnFlightId,
			List<Passenger> passengers, FLIGHTTYPE tripType, String token);

	Mono<Ticket> getByPnr(String pnr);

//	Flux<Ticket> historyByEmail(String email);
	
//	Flux<Ticket> history(Authentication authentication);
	Flux<BookingHistoryResponse> history(Authentication authentication);

	Mono<String> cancelByPnr(String pnr, String token);
}	